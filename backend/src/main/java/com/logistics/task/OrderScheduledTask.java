package com.logistics.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.entity.OrderMain;
import com.logistics.mapper.OrderMainMapper;
import com.logistics.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务 - 自动关闭超时订单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduledTask {

    private final OrderMainMapper orderMainMapper;
    private final OrderService orderService;

    /**
     * 每5分钟扫描一次，自动关闭超过24小时未确认的订单
     */
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void closeTimeoutOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusHours(24);
        // 查找超过24小时仍为"待确认(0)"状态的订单
        List<OrderMain> timeoutOrders = orderMainMapper.selectList(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getOrderStatus, 0)
                        .eq(OrderMain::getDeleteFlag, 0)
                        .lt(OrderMain::getCreateTime, timeout));

        if (timeoutOrders.isEmpty())
            return;

        log.info("定时任务：发现 {} 个超时未确认订单，开始自动关闭", timeoutOrders.size());

        for (OrderMain order : timeoutOrders) {
            try {
                // 更新状态为已关闭(99)
                OrderMain update = new OrderMain();
                update.setOrderId(order.getOrderId());
                update.setOrderStatus(99);
                update.setCloseTime(LocalDateTime.now());
                update.setCloseReason("系统自动关闭：超过24小时未确认");
                orderMainMapper.updateById(update);

                // 恢复库存
                orderService.restoreStock(order.getOrderId());

                // 记录轨迹
                orderService.addTrack(order.getOrderId(), 99,
                        "系统自动关闭：订单超过24小时未确认", 0L, "系统");

                log.info("自动关闭订单：{}", order.getOrderNo());
            } catch (Exception e) {
                log.error("自动关闭订单 {} 失败：{}", order.getOrderNo(), e.getMessage());
            }
        }
    }

    /**
     * 每小时扫描一次，自动关闭超过48小时未分配物流的订单
     */
    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    public void closeUnshippedOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusHours(48);
        List<OrderMain> list = orderMainMapper.selectList(
                new LambdaQueryWrapper<OrderMain>()
                        .eq(OrderMain::getOrderStatus, 1) // 已确认但未分配物流
                        .eq(OrderMain::getDeleteFlag, 0)
                        .lt(OrderMain::getCreateTime, timeout));

        if (list.isEmpty())
            return;

        log.info("定时任务：发现 {} 个超时未发货订单，开始自动关闭", list.size());

        for (OrderMain order : list) {
            try {
                OrderMain update = new OrderMain();
                update.setOrderId(order.getOrderId());
                update.setOrderStatus(99);
                update.setCloseTime(LocalDateTime.now());
                update.setCloseReason("系统自动关闭：超过48小时未分配物流");
                orderMainMapper.updateById(update);

                orderService.restoreStock(order.getOrderId());
                orderService.addTrack(order.getOrderId(), 99,
                        "系统自动关闭：订单超过48小时未发货", 0L, "系统");

                log.info("自动关闭未发货订单：{}", order.getOrderNo());
            } catch (Exception e) {
                log.error("自动关闭订单 {} 失败：{}", order.getOrderNo(), e.getMessage());
            }
        }
    }
}
