package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告附件表
 */
@Data
@TableName("notice_attachment")
public class NoticeAttachment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long naId;

    private Long noticeId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}
