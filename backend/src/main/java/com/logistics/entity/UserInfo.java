package com.logistics.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户核心信息表
 */
@Data
@TableName("user_info")
public class UserInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;

    private String username;

    private String password;

    /** 角色类型：1=管理员，2=供应方，3=物流方，4=消费者 */
    private Integer roleType;

    private String phone;
    private String email;
    private String qq;
    private String avatar;

    /** 性别：0=未知，1=男，2=女 */
    private Integer gender;

    /** 账号状态：0=禁用，1=启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long createBy;

    @TableLogic
    private Integer deleteFlag;
}
