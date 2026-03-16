package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告主表
 */
@Data
@TableName("notice")
public class Notice {

    @TableId(type = IdType.ASSIGN_ID)
    private Long noticeId;

    private String title;
    private String content;
    private Long publisherId;
    private String publisherName;

    /** 是否置顶：0=否，1=是 */
    private Integer isTop;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 公告状态：0=删除，1=正常 */
    private Integer status;
}
