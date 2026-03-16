package com.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.aop.OperLog;
import com.logistics.common.Result;
import com.logistics.entity.SupplyInfo;
import com.logistics.entity.TraceInfo;
import com.logistics.security.SecurityUtil;
import com.logistics.service.SupplyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/supply")
@RequiredArgsConstructor
public class SupplyController {

    private final SupplyService supplyService;

    // ========== 供应方操作 ==========

    @Data
    static class SupplyRequest {
        private SupplyInfo supply;
        private TraceInfo trace;
    }

    @OperLog(module = "供应管理", type = "CREATE", desc = "发布供应信息")
    @PostMapping("/add")
    public Result<SupplyInfo> add(@RequestBody SupplyRequest req) {
        req.getSupply().setSupplierId(SecurityUtil.getCurrentUserId());
        req.getSupply().setSupplierName(SecurityUtil.getCurrentUser().getUsername());
        return Result.ok(supplyService.addSupply(req.getSupply(), req.getTrace()));
    }

    @OperLog(module = "供应管理", type = "UPDATE", desc = "编辑供应信息")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody SupplyInfo supply) {
        supplyService.updateSupply(supply);
        return Result.ok();
    }

    @GetMapping("/list")
    public Result<List<SupplyInfo>> supplierList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        Long supplierId = SecurityUtil.getCurrentUserId();
        Page<SupplyInfo> result = supplyService.supplierList(page, size, supplierId, productName, status);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "供应管理", type = "UPDATE", desc = "供应信息上架")
    @PutMapping("/online")
    public Result<Void> online(@RequestParam Long supplyId) {
        supplyService.online(supplyId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "供应管理", type = "UPDATE", desc = "供应信息下架")
    @PutMapping("/offline")
    public Result<Void> offline(@RequestParam Long supplyId) {
        supplyService.offline(supplyId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    // ========== 公共接口 ==========

    @GetMapping("/common/list")
    public Result<List<SupplyInfo>> commonList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String productionPlace,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        Page<SupplyInfo> result = supplyService.commonList(page, size, productName, category, productionPlace, minPrice,
                maxPrice);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @GetMapping("/common/detail")
    public Result<Map<String, Object>> detail(
            @RequestParam(required = false) Long supplyId,
            @RequestParam(required = false) String traceCode) {
        return Result.ok(supplyService.getDetail(supplyId, traceCode));
    }

    // ========== 管理员操作 ==========

    @GetMapping("/admin/list")
    public Result<List<SupplyInfo>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer auditStatus) {
        Page<SupplyInfo> result = supplyService.adminList(page, size, productName, auditStatus);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "供应管理", type = "AUDIT", desc = "管理员审核供应信息")
    @PutMapping("/admin/audit")
    public Result<Void> audit(@RequestParam Long supplyId,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String auditRemark) {
        supplyService.audit(supplyId, auditStatus, auditRemark);
        return Result.ok();
    }

    @OperLog(module = "供应管理", type = "UPDATE", desc = "管理员强制下架")
    @PutMapping("/admin/forceOffline")
    public Result<Void> forceOffline(@RequestParam Long supplyId) {
        supplyService.forceOffline(supplyId);
        return Result.ok();
    }
}
