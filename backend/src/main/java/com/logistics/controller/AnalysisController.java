package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.ok(analysisService.overview());
    }

    @GetMapping("/order/trend")
    public Result<List<Map<String, Object>>> orderTrend() {
        return Result.ok(analysisService.orderTrend());
    }

    @GetMapping("/category/distribution")
    public Result<List<Map<String, Object>>> categoryDistribution() {
        return Result.ok(analysisService.categoryDistribution());
    }

    @GetMapping("/place/ranking")
    public Result<List<Map<String, Object>>> placeRanking() {
        return Result.ok(analysisService.placeRanking());
    }

    @GetMapping("/supplier/ranking")
    public Result<List<Map<String, Object>>> supplierRanking() {
        return Result.ok(analysisService.supplierRanking());
    }

    @GetMapping("/inventory/warning")
    public Result<List<Map<String, Object>>> inventoryWarning() {
        return Result.ok(analysisService.inventoryWarning());
    }

    @GetMapping("/inventory/turnover")
    public Result<List<Map<String, Object>>> inventoryTurnover() {
        return Result.ok(analysisService.inventoryTurnover());
    }

    @GetMapping("/cold-chain-warning")
    public Result<Map<String, Object>> coldChainWarning() {
        return Result.ok(analysisService.coldChainWarning());
    }
}
