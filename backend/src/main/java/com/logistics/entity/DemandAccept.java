package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("demand_accept")
public class DemandAccept {

    @TableId(type = IdType.ASSIGN_ID)
    private Long acceptId;

    private Long demandId;
    private Long acceptSupplierId;
    private String supplierName;
    private String acceptScheme;
    private BigDecimal offer;
    private LocalDate deliveryTime;
    private String qualificationInfo;

    /** 承接状态: 0待审核 1已确认 2已驳回 */
    private Integer acceptStatus;
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
