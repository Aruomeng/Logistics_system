package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表
 */
@Data
@TableName("order_main")
public class OrderMain {

    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId;

    /** 订单编号，全局唯一 */
    private String orderNo;

    private Long consumerId;
    private String consumerName;
    private Long supplierId;
    private String supplierName;
    private Long logisticsId;
    private String logisticsName;

    /** 订单总金额 */
    private BigDecimal totalAmount;
    /** 商品金额 */
    private BigDecimal productAmount;
    /** 物流费用 */
    private BigDecimal logisticsAmount;

    /** 支付状态：0=未支付，1=已支付，2=已退款 */
    private Integer payStatus;

    /** 订单状态：0=待支付，1=待发货，2=待揽收，3=运输中，4=待收货，5=已完成，99=已关闭 */
    private Integer orderStatus;

    private String deliveryAddress;
    private String deliveryNo;

    private LocalDateTime payTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private String closeReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleteFlag;
}
