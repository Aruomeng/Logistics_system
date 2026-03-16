package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单轨迹表
 */
@Data
@TableName("order_track")
public class OrderTrack {

    @TableId(type = IdType.ASSIGN_ID)
    private Long trackId;

    private Long orderId;
    private Integer orderStatus;
    private String changeContent;
    private Long operatorId;
    private String operatorName;
    private Integer operatorRole;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
