package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 溯源信息表
 */
@Data
@TableName("trace_info")
public class TraceInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long traceId;

    private Long supplyId;

    /** 种子/种苗信息 */
    private String seedInfo;

    /** 施肥信息 */
    private String fertilizerInfo;

    /** 农药使用信息 */
    private String pesticideInfo;

    /** 采收信息 */
    private String harvestInfo;

    /** 质检报告信息 */
    private String inspectionInfo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
