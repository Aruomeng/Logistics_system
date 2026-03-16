package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应信息主表
 */
@Data
@TableName("supply_info")
public class SupplyInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long supplyId;

    private String productName;

    /** 产品品类：1=农产品，2=生产物料 */
    private String category;

    /** 产品子品类，如：蔬菜、水果、化肥 */
    private String subCategory;

    private String productionPlace;
    private LocalDate productionDate;

    /** 保质期（天） */
    private Integer shelfLife;

    /** 单价（元/公斤） */
    private BigDecimal price;

    /** 系统推荐价格 */
    private BigDecimal recommendPrice;

    /** 库存数量（公斤） */
    private BigDecimal stock;

    private Long supplierId;
    private String supplierName;

    /** 唯一溯源码编号 */
    private String traceCode;

    /** 溯源码图片存储地址 */
    private String traceCodeUrl;

    /** 审核状态：0=待审核，1=审核通过，2=审核驳回 */
    private Integer auditStatus;

    /** 上下架状态：0=下架，1=上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
