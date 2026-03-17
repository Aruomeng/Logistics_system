package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.logistics.common.BusinessException;
import com.logistics.entity.*;
import com.logistics.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<OrderMainMapper, OrderMain> {

    private final OrderDetailMapper orderDetailMapper;
    private final OrderTrackMapper orderTrackMapper;
    private final SupplyInfoMapper supplyInfoMapper;

    /**
     * 消费者下单
     */
    @Transactional
    public OrderMain createOrder(Long consumerId, String consumerName,
            List<OrderDetail> items, String deliveryAddress) {
        if (items == null || items.isEmpty())
            throw new BusinessException("订单明细不能为空");

        // 计算总金额、校验库存
        BigDecimal totalAmount = BigDecimal.ZERO;
        Long supplierId = null;
        String supplierName = null;

        for (OrderDetail item : items) {
            SupplyInfo supply = supplyInfoMapper.selectById(item.getSupplyId());
            if (supply == null || supply.getStatus() != 1)
                throw new BusinessException("商品 " + item.getProductName() + " 不可购买");
            if (supply.getStock().compareTo(item.getQuantity()) < 0)
                throw new BusinessException("商品 " + item.getProductName() + " 库存不足");

            item.setProductName(supply.getProductName());
            item.setUnitPrice(supply.getPrice());
            item.setTotalPrice(supply.getPrice().multiply(item.getQuantity()));
            item.setTraceCode(supply.getTraceCode());
            totalAmount = totalAmount.add(item.getTotalPrice());

            // 校验是否同一供应方
            if (supplierId == null) {
                supplierId = supply.getSupplierId();
                supplierName = supply.getSupplierName();
            } else if (!supplierId.equals(supply.getSupplierId())) {
                throw new BusinessException("一个订单只能包含同一供应方的商品");
            }

            // 扣减库存
            supply.setStock(supply.getStock().subtract(item.getQuantity()));
            supplyInfoMapper.updateById(supply);
        }

        // 创建主订单
        OrderMain order = new OrderMain();
        order.setOrderNo(generateOrderNo());
        order.setConsumerId(consumerId);
        order.setConsumerName(consumerName);
        order.setSupplierId(supplierId);
        order.setSupplierName(supplierName);
        order.setTotalAmount(totalAmount);
        order.setProductAmount(totalAmount);
        order.setDeliveryAddress(deliveryAddress);
        order.setOrderStatus(0); // 待确认
        order.setDeleteFlag(0);
        save(order);

        // 保存明细
        for (OrderDetail item : items) {
            item.setOrderId(order.getOrderId());
            orderDetailMapper.insert(item);
        }

        // 记录轨迹
        addTrack(order.getOrderId(), 0, "消费者下单", consumerId, consumerName);

        return order;
    }

    /**
     * 供应方确认订单
     */
    @Transactional
    public void supplierConfirm(Long orderId, Long supplierId) {
        OrderMain order = getById(orderId);
        validateOrder(order, supplierId, "SUPPLIER", 0);
        updateStatus(orderId, 1);
        addTrack(orderId, 1, "供应方已确认订单", supplierId, order.getSupplierName());
    }

    /**
     * 供应方拒绝订单
     */
    @Transactional
    public void supplierReject(Long orderId, Long supplierId, String reason) {
        OrderMain order = getById(orderId);
        validateOrder(order, supplierId, "SUPPLIER", 0);
        updateStatus(orderId, 99); // 已关闭
        addTrack(orderId, 99, "供应方拒绝: " + reason, supplierId, order.getSupplierName());
        // 恢复库存
        restoreStock(orderId);
    }

    /**
     * 分配物流方
     */
    @Transactional
    public void assignLogistics(Long orderId, Long logisticsId, String logisticsName, Long operatorId) {
        OrderMain order = getById(orderId);
        if (order == null || order.getOrderStatus() != 1)
            throw new BusinessException("订单状态不允许分配物流");
        OrderMain update = new OrderMain();
        update.setOrderId(orderId);
        update.setLogisticsId(logisticsId);
        update.setLogisticsName(logisticsName);
        update.setOrderStatus(2); // 待揽收
        updateById(update);
        addTrack(orderId, 2, "已分配物流: " + logisticsName, operatorId, "系统");
    }

    /**
     * 供应方确认订单并分配物流（合并操作）
     * 状态流转: 0(待确认) -> 2(待揽收)
     */
    @Transactional
    public void supplierConfirmAndAssign(Long orderId, Long supplierId,
            Long logisticsId, String logisticsName) {
        OrderMain order = getById(orderId);
        validateOrder(order, supplierId, "SUPPLIER", 0);

        OrderMain update = new OrderMain();
        update.setOrderId(orderId);
        update.setOrderStatus(2); // 直接到待揽收
        update.setLogisticsId(logisticsId);
        update.setLogisticsName(logisticsName);
        updateById(update);

        addTrack(orderId, 1, "供应方已确认订单", supplierId, order.getSupplierName());
        addTrack(orderId, 2, "已分配物流: " + logisticsName, supplierId, order.getSupplierName());
    }

    /**
     * 物流方揽收
     * 状态流转: 2(待揽收) -> 3(运输中)
     */
    @Transactional
    public void logisticsPickup(Long orderId, Long logisticsId) {
        OrderMain order = getById(orderId);
        validateOrder(order, logisticsId, "LOGISTICS", 2);
        updateStatus(orderId, 3);
        addTrack(orderId, 3, "物流方已揽收，开始运输", logisticsId, order.getLogisticsName());
    }

    /**
     * 物流方更新运输状态
     */
    @Transactional
    public void logisticsUpdate(Long orderId, Long logisticsId, String trackInfo) {
        OrderMain order = getById(orderId);
        validateOrder(order, logisticsId, "LOGISTICS", 3);
        addTrack(orderId, 3, trackInfo, logisticsId, order.getLogisticsName());
    }

    /**
     * 物流方标记送达
     */
    @Transactional
    public void logisticsDelivered(Long orderId, Long logisticsId) {
        OrderMain order = getById(orderId);
        validateOrder(order, logisticsId, "LOGISTICS", 3);
        updateStatus(orderId, 4); // 待收货
        addTrack(orderId, 4, "物流已送达", logisticsId, order.getLogisticsName());
    }

    /**
     * 消费者确认收货
     */
    @Transactional
    public void consumerConfirm(Long orderId, Long consumerId) {
        OrderMain order = getById(orderId);
        validateOrder(order, consumerId, "CONSUMER", 4);
        updateStatus(orderId, 5); // 已完成
        addTrack(orderId, 5, "消费者已确认收货", consumerId, order.getConsumerName());
    }

    /**
     * 消费者取消订单
     */
    @Transactional
    public void consumerCancel(Long orderId, Long consumerId) {
        OrderMain order = getById(orderId);
        if (order == null || !order.getConsumerId().equals(consumerId))
            throw new BusinessException("订单不存在");
        if (order.getOrderStatus() > 2)
            throw new BusinessException("订单已进入物流阶段，无法取消");
        updateStatus(orderId, 99);
        addTrack(orderId, 99, "消费者取消订单", consumerId, order.getConsumerName());
        restoreStock(orderId);
    }

    // ========== 查询 ==========

    /** 消费者订单列表 */
    public Page<OrderMain> consumerList(int page, int size, Long consumerId, Integer status) {
        return queryPage(page, size, "consumer", consumerId, status, null);
    }

    /** 供应方订单列表 */
    public Page<OrderMain> supplierList(int page, int size, Long supplierId, Integer status) {
        return queryPage(page, size, "supplier", supplierId, status, null);
    }

    /** 物流方订单列表 */
    public Page<OrderMain> logisticsList(int page, int size, Long logisticsId, Integer status) {
        return queryPage(page, size, "logistics", logisticsId, status, null);
    }

    /** 管理员订单列表 */
    public Page<OrderMain> adminList(int page, int size, Integer status, String orderNo) {
        return queryPage(page, size, "admin", null, status, orderNo);
    }

    /** 订单详情（含明细 + 轨迹） */
    public OrderMain getDetail(Long orderId) {
        OrderMain order = getById(orderId);
        if (order == null)
            throw new BusinessException("订单不存在");
        return order;
    }

    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderDetailMapper.selectList(
                new LambdaQueryWrapper<OrderDetail>().eq(OrderDetail::getOrderId, orderId));
    }

    public List<OrderTrack> getOrderTracks(Long orderId) {
        return orderTrackMapper.selectList(
                new LambdaQueryWrapper<OrderTrack>().eq(OrderTrack::getOrderId, orderId)
                        .orderByAsc(OrderTrack::getCreateTime));
    }

    // ========== 内部方法 ==========

    private Page<OrderMain> queryPage(int page, int size, String role, Long userId, Integer status, String orderNo) {
        LambdaQueryWrapper<OrderMain> w = new LambdaQueryWrapper<>();
        w.eq(OrderMain::getDeleteFlag, 0);
        switch (role) {
            case "consumer" -> w.eq(OrderMain::getConsumerId, userId);
            case "supplier" -> w.eq(OrderMain::getSupplierId, userId);
            case "logistics" -> w.eq(OrderMain::getLogisticsId, userId);
        }
        w.eq(status != null, OrderMain::getOrderStatus, status)
                .like(StringUtils.hasText(orderNo), OrderMain::getOrderNo, orderNo)
                .orderByDesc(OrderMain::getCreateTime);
        return page(new Page<>(page, size), w);
    }

    private void validateOrder(OrderMain order, Long userId, String role, int expectedStatus) {
        if (order == null)
            throw new BusinessException("订单不存在");
        boolean match = switch (role) {
            case "SUPPLIER" -> order.getSupplierId().equals(userId);
            case "LOGISTICS" -> order.getLogisticsId() != null && order.getLogisticsId().equals(userId);
            case "CONSUMER" -> order.getConsumerId().equals(userId);
            default -> false;
        };
        if (!match)
            throw new BusinessException("无权操作此订单");
        if (order.getOrderStatus() != expectedStatus)
            throw new BusinessException("当前订单状态不允许此操作");
    }

    private void updateStatus(Long orderId, int orderStatus) {
        OrderMain u = new OrderMain();
        u.setOrderId(orderId);
        u.setOrderStatus(orderStatus);
        if (orderStatus == 5)
            u.setFinishTime(LocalDateTime.now());
        updateById(u);
    }

    public void addTrack(Long orderId, int orderStatus, String changeContent, Long operatorId, String operatorName) {
        OrderTrack track = new OrderTrack();
        track.setOrderId(orderId);
        track.setOrderStatus(orderStatus);
        track.setChangeContent(changeContent);
        track.setOperatorId(operatorId);
        track.setOperatorName(operatorName);
        track.setOperatorRole(0);
        orderTrackMapper.insert(track);
    }

    public void restoreStock(Long orderId) {
        List<OrderDetail> details = getOrderDetails(orderId);
        for (OrderDetail d : details) {
            SupplyInfo supply = supplyInfoMapper.selectById(d.getSupplyId());
            if (supply != null) {
                supply.setStock(supply.getStock().add(d.getQuantity()));
                supplyInfoMapper.updateById(supply);
            }
        }
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + date + String.format("%04d", new Random().nextInt(10000));
    }
}
