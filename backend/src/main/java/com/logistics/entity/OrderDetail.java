package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细表
 */
@Data
@TableName("order_detail")
public class OrderDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long detailId;

    private Long orderId;
    private Long supplyId;
    private String productName;

    /** 单价（元/公斤） */
    private BigDecimal unitPrice;

    /** 购买数量（公斤） */
    private BigDecimal quantity;

    /** 商品总价 */
    private BigDecimal totalPrice;

    /** 产品溯源码 */
    private String traceCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
