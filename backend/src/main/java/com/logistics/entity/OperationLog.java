package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long logId;

    private Long operatorId;
    private String operatorName;
    private Integer operatorRole;

    /** 操作类型：LOGIN, CREATE, UPDATE, DELETE, EXPORT 等 */
    private String operationType;

    /** 操作模块 */
    private String module;

    /** 操作描述 */
    private String description;

    /** 请求方法 */
    private String method;

    /** 请求路径 */
    private String requestUrl;

    /** 请求参数 */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String requestParams;

    /** 响应结果 */
    private String responseResult;

    /** 操作IP */
    private String operatorIp;

    /** 耗时（ms） */
    private Long costTime;

    /** 操作状态：0=失败，1=成功 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
