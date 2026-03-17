package com.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.aop.OperLog;
import com.logistics.common.Result;
import com.logistics.entity.OrderDetail;
import com.logistics.entity.OrderMain;
import com.logistics.entity.OrderTrack;
import com.logistics.security.SecurityUtil;
import com.logistics.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ========== 消费者操作 ==========

    @Data
    static class CreateOrderRequest {
        private List<OrderDetail> items;
        private String deliveryAddress;
    }

    @OperLog(module = "订单管理", type = "CREATE", desc = "创建订单")
    @PostMapping("/create")
    public Result<OrderMain> create(@RequestBody CreateOrderRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        String username = SecurityUtil.getCurrentUser().getUsername();
        return Result.ok(
                orderService.createOrder(userId, username, req.getItems(), req.getDeliveryAddress()));
    }

    @GetMapping("/consumer/list")
    public Result<List<OrderMain>> consumerList(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<OrderMain> result = orderService.consumerList(page, size, SecurityUtil.getCurrentUserId(), status);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "消费者确认收货")
    @PutMapping("/consumer/confirm")
    public Result<Void> consumerConfirm(@RequestParam Long orderId) {
        orderService.consumerConfirm(orderId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "消费者取消订单")
    @PutMapping("/consumer/cancel")
    public Result<Void> consumerCancel(@RequestParam Long orderId) {
        orderService.consumerCancel(orderId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    // ========== 供应方操作 ==========

    @GetMapping("/supplier/list")
    public Result<List<OrderMain>> supplierList(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<OrderMain> result = orderService.supplierList(page, size, SecurityUtil.getCurrentUserId(), status);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "供应方确认订单")
    @PutMapping("/supplier/confirm")
    public Result<Void> supplierConfirm(@RequestParam Long orderId) {
        orderService.supplierConfirm(orderId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "供应方确认并分配物流")
    @PutMapping("/supplier/confirmAndAssign")
    public Result<Void> supplierConfirmAndAssign(@RequestParam Long orderId,
            @RequestParam Long logisticsId, @RequestParam String logisticsName) {
        orderService.supplierConfirmAndAssign(orderId, SecurityUtil.getCurrentUserId(),
                logisticsId, logisticsName);
        return Result.ok();
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "供应方拒绝订单")
    @PutMapping("/supplier/reject")
    public Result<Void> supplierReject(@RequestParam Long orderId, @RequestParam(required = false) String reason) {
        orderService.supplierReject(orderId, SecurityUtil.getCurrentUserId(), reason);
        return Result.ok();
    }

    // ========== 物流方操作 ==========

    @GetMapping("/logistics/list")
    public Result<List<OrderMain>> logisticsList(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Page<OrderMain> result = orderService.logisticsList(page, size, SecurityUtil.getCurrentUserId(), status);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "物流方揽收")
    @PutMapping("/logistics/pickup")
    public Result<Void> logisticsPickup(@RequestParam Long orderId) {
        orderService.logisticsPickup(orderId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "物流更新轨迹")
    @PutMapping("/logistics/update")
    public Result<Void> logisticsUpdate(@RequestParam Long orderId, @RequestParam String trackInfo) {
        orderService.logisticsUpdate(orderId, SecurityUtil.getCurrentUserId(), trackInfo);
        return Result.ok();
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "物流标记已送达")
    @PutMapping("/logistics/delivered")
    public Result<Void> logisticsDelivered(@RequestParam Long orderId) {
        orderService.logisticsDelivered(orderId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    // ========== 管理员操作 ==========

    @GetMapping("/admin/list")
    public Result<List<OrderMain>> adminList(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status, @RequestParam(required = false) String orderNo) {
        Page<OrderMain> result = orderService.adminList(page, size, status, orderNo);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "订单管理", type = "UPDATE", desc = "管理员分配物流")
    @PutMapping("/admin/assignLogistics")
    public Result<Void> assignLogistics(@RequestParam Long orderId, @RequestParam Long logisticsId,
            @RequestParam String logisticsName) {
        orderService.assignLogistics(orderId, logisticsId, logisticsName, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    // ========== 公共接口 ==========

    @GetMapping("/detail")
    public Result<Map<String, Object>> detail(@RequestParam Long orderId) {
        OrderMain order = orderService.getDetail(orderId);
        List<OrderDetail> details = orderService.getOrderDetails(orderId);
        List<OrderTrack> tracks = orderService.getOrderTracks(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("details", details);
        data.put("tracks", tracks);
        return Result.ok(data);
    }
}
