package com.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.aop.OperLog;
import com.logistics.common.Result;
import com.logistics.entity.DemandAccept;
import com.logistics.entity.DemandMain;
import com.logistics.security.SecurityUtil;
import com.logistics.service.DemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/demand")
@RequiredArgsConstructor
public class DemandController {

    private final DemandService demandService;

    // ========== 采购方（消费者）操作 ==========

    @OperLog(module = "需求管理", type = "CREATE", desc = "发布采购需求")
    @PostMapping("/add")
    public Result<DemandMain> add(@RequestBody DemandMain demand) {
        demand.setPublisherId(SecurityUtil.getCurrentUserId());
        demand.setPublisherName(SecurityUtil.getCurrentUser().getUsername());
        return Result.ok(demandService.addDemand(demand));
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "编辑需求")
    @PutMapping("/update")
    public Result<Void> update(@RequestBody DemandMain demand) {
        demandService.updateDemand(demand, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @GetMapping("/my/list")
    public Result<List<DemandMain>> myList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer demandStatus) {
        Page<DemandMain> result = demandService.myList(page, size, SecurityUtil.getCurrentUserId(), title,
                demandStatus);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "需求上架")
    @PutMapping("/online")
    public Result<Void> online(@RequestParam Long demandId) {
        demandService.online(demandId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "需求下架")
    @PutMapping("/offline")
    public Result<Void> offline(@RequestParam Long demandId) {
        demandService.offline(demandId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "关闭需求")
    @PutMapping("/close")
    public Result<Void> close(@RequestParam Long demandId,
            @RequestParam(required = false) String closeReason) {
        demandService.close(demandId, SecurityUtil.getCurrentUserId(), closeReason);
        return Result.ok();
    }

    // ========== 公共接口 ==========

    @GetMapping("/common/list")
    public Result<List<DemandMain>> commonList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectTimeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectTimeEnd) {
        Page<DemandMain> result = demandService.commonList(page, size, title, category, region, expectTimeStart,
                expectTimeEnd);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @GetMapping("/common/detail")
    public Result<Map<String, Object>> detail(@RequestParam Long demandId) {
        return Result.ok(demandService.getDetail(demandId));
    }

    // ========== 供应方承接 ==========

    @OperLog(module = "需求管理", type = "CREATE", desc = "供应方发起承接")
    @PostMapping("/accept/add")
    public Result<DemandAccept> addAccept(@RequestBody DemandAccept accept) {
        accept.setAcceptSupplierId(SecurityUtil.getCurrentUserId());
        accept.setSupplierName(SecurityUtil.getCurrentUser().getUsername());
        return Result.ok(demandService.addAccept(accept));
    }

    @GetMapping("/accept/list")
    public Result<List<DemandAccept>> acceptList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long demandId,
            @RequestParam(required = false) Integer acceptStatus) {
        Page<DemandAccept> result = demandService.acceptList(page, size, demandId, acceptStatus);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "确认承接")
    @PutMapping("/accept/confirm")
    public Result<Void> confirmAccept(@RequestParam Long acceptId) {
        demandService.confirmAccept(acceptId, SecurityUtil.getCurrentUserId());
        return Result.ok();
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "驳回承接")
    @PutMapping("/accept/reject")
    public Result<Void> rejectAccept(@RequestParam Long acceptId,
            @RequestParam(required = false) String rejectReason) {
        demandService.rejectAccept(acceptId, SecurityUtil.getCurrentUserId(), rejectReason);
        return Result.ok();
    }

    // ========== 供需匹配 ==========

    @GetMapping("/match")
    public Result<?> match(@RequestParam(required = false) Long demandId,
            @RequestParam(required = false) Long supplyId) {
        if (demandId != null) {
            return Result.ok(demandService.matchSupply(demandId));
        } else if (supplyId != null) {
            return Result.ok(demandService.matchDemand(supplyId));
        }
        return Result.fail("请提供 demandId 或 supplyId");
    }

    // ========== 管理员操作 ==========

    @OperLog(module = "需求管理", type = "AUDIT", desc = "管理员审核需求")
    @PutMapping("/admin/audit")
    public Result<Void> audit(@RequestParam Long demandId,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String auditRemark) {
        demandService.audit(demandId, auditStatus, auditRemark);
        return Result.ok();
    }

    @GetMapping("/admin/list")
    public Result<List<DemandMain>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer demandStatus,
            @RequestParam(required = false) String publisherName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<DemandMain> result = demandService.adminList(page, size, title, demandStatus, publisherName, startTime,
                endTime);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "需求管理", type = "UPDATE", desc = "管理员强制下架需求")
    @PutMapping("/admin/forceOffline")
    public Result<Void> forceOffline(@RequestParam Long demandId) {
        demandService.forceOffline(demandId);
        return Result.ok();
    }
}
