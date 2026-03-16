package com.logistics.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.Result;
import com.logistics.entity.OperationLog;
import com.logistics.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogMapper logMapper;

    @GetMapping("/list")
    public Result<List<OperationLog>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operatorName) {
        LambdaQueryWrapper<OperationLog> w = new LambdaQueryWrapper<>();
        w.like(StringUtils.hasText(module), OperationLog::getModule, module)
                .like(StringUtils.hasText(operatorName), OperationLog::getOperatorName, operatorName)
                .orderByDesc(OperationLog::getCreateTime);
        Page<OperationLog> result = logMapper.selectPage(new Page<>(page, size), w);
        return Result.ok(result.getRecords(), result.getTotal());
    }
}
