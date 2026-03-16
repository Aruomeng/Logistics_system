package com.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.aop.OperLog;
import com.logistics.common.Result;
import com.logistics.entity.UserInfo;
import com.logistics.security.SecurityUtil;
import com.logistics.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final com.logistics.controller.CaptchaController captchaController;

    // ========== 登录/登出 ==========

    @Data
    static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
        private String captchaKey;
        private String captchaCode;
    }

    @OperLog(module = "用户管理", type = "LOGIN", desc = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        // 校验验证码
        if (req.getCaptchaKey() != null && !req.getCaptchaKey().isEmpty()) {
            if (!captchaController.verify(req.getCaptchaKey(), req.getCaptchaCode())) {
                return Result.fail(400, "验证码错误");
            }
        }
        Map<String, Object> data = userService.login(req.getUsername(), req.getPassword());
        return Result.ok(data);
    }

    @OperLog(module = "用户管理", type = "LOGOUT", desc = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId != null) {
            userService.logout(userId);
        }
        return Result.ok();
    }

    // ========== 个人信息 ==========

    @GetMapping("/info")
    public Result<UserInfo> getInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return Result.ok(userService.getUserInfo(userId));
    }

    @OperLog(module = "用户管理", type = "UPDATE", desc = "修改个人信息")
    @PutMapping("/info/update")
    public Result<Void> updateInfo(@RequestBody UserInfo user) {
        user.setUserId(SecurityUtil.getCurrentUserId());
        userService.updateUserInfo(user);
        return Result.ok();
    }

    @Data
    static class PasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPwd;
        @NotBlank(message = "新密码不能为空")
        private String newPwd;
    }

    @OperLog(module = "用户管理", type = "UPDATE", desc = "修改密码")
    @PutMapping("/password/modify")
    public Result<Void> modifyPassword(@RequestBody PasswordRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.modifyPassword(userId, req.getOldPwd(), req.getNewPwd());
        return Result.ok();
    }

    @Data
    static class VerifyPasswordRequest {
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @OperLog(module = "安全中心", type = "AUTH", desc = "高危操作二次验证")
    @PostMapping("/password/verify")
    public Result<Void> verifyPassword(@RequestBody VerifyPasswordRequest req) {
        Long userId = SecurityUtil.getCurrentUserId();
        userService.verifyPassword(userId, req.getPassword());
        return Result.ok();
    }

    // ========== 管理员用户管理 ==========

    @GetMapping("/admin/list")
    public Result<List<UserInfo>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer roleType,
            @RequestParam(required = false) Integer status) {
        Page<UserInfo> result = userService.listUsers(page, size, username, roleType, status);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "用户管理", type = "CREATE", desc = "管理员新增用户")
    @PostMapping("/admin/add")
    public Result<UserInfo> addUser(@RequestBody UserInfo user) {
        Long createBy = SecurityUtil.getCurrentUserId();
        return Result.ok(userService.addUser(user, createBy));
    }

    @OperLog(module = "用户管理", type = "UPDATE", desc = "管理员修改用户")
    @PutMapping("/admin/update")
    public Result<Void> adminUpdateUser(@RequestBody UserInfo user) {
        userService.adminUpdateUser(user);
        return Result.ok();
    }

    @OperLog(module = "用户管理", type = "DELETE", desc = "管理员删除用户")
    @DeleteMapping("/admin/delete")
    public Result<Void> deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return Result.ok();
    }

    @OperLog(module = "用户管理", type = "UPDATE", desc = "管理员重置密码")
    @PutMapping("/admin/password/reset")
    public Result<Void> resetPassword(@RequestParam Long userId) {
        userService.resetPassword(userId);
        return Result.ok();
    }
}
