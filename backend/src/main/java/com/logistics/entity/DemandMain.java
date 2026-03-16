package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("demand_main")
public class DemandMain {

    @TableId(type = IdType.ASSIGN_ID)
    private Long demandId;

    private String demandNo;
    private String title;
    private String description;
    private String category;
    private String subCategory;
    private BigDecimal quantity;
    private BigDecimal expectPrice;
    private LocalDate expectTime;
    private String address;
    private String qualificationRequire;
    private Long publisherId;
    private String publisherName;

    /** 审核状态: 0待审核 1通过 2驳回 */
    private Integer auditStatus;
    /** 需求状态: 0待发布 1已发布 2承接中 3已对接 4已完成 99已关闭 */
    private Integer demandStatus;
    private Integer isTop;
    private String auditRemark;
    private String closeReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
