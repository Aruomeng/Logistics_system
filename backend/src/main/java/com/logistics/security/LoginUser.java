package com.logistics.security;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录用户信息，存储在 SecurityContext 的 details 中
 */
@Data
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String username;
    private Integer roleType;
}
