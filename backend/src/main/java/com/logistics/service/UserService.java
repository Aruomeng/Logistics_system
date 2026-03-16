package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.logistics.common.BusinessException;
import com.logistics.common.JwtUtil;
import com.logistics.entity.UserInfo;
import com.logistics.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserInfoMapper, UserInfo> {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        // 查询用户
        UserInfo user = lambdaQuery()
                .eq(UserInfo::getUsername, username)
                .eq(UserInfo::getDeleteFlag, 0)
                .one();
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        // 校验状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        // 生成 Token
        String token = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername(), user.getRoleType());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 存储到 Redis（支持单端登录 & 踢下线）
        @SuppressWarnings("null")
        var ops = redisTemplate.opsForValue();
        ops.set("user:token:" + user.getUserId(), token, 2, TimeUnit.HOURS);
        ops.set("user:refresh:" + user.getUserId(), refreshToken, 7, TimeUnit.DAYS);

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("roleType", user.getRoleType());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("gender", user.getGender());
        result.put("userInfo", userInfo);

        return result;
    }

    /**
     * 用户登出
     */
    public void logout(Long userId) {
        redisTemplate.delete("user:token:" + userId);
        redisTemplate.delete("user:refresh:" + userId);
    }

    /**
     * 获取个人信息
     */
    public UserInfo getUserInfo(Long userId) {
        UserInfo user = getById(userId);
        if (user == null || user.getDeleteFlag() == 1) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return user;
    }

    /**
     * 更新个人信息
     */
    public void updateUserInfo(UserInfo user) {
        UserInfo existing = getById(user.getUserId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        // 只允许更新部分字段
        UserInfo update = new UserInfo();
        update.setUserId(user.getUserId());
        update.setPhone(user.getPhone());
        update.setEmail(user.getEmail());
        update.setQq(user.getQq());
        update.setAvatar(user.getAvatar());
        update.setGender(user.getGender());
        updateById(update);
    }

    /**
     * 修改密码
     */
    public void modifyPassword(Long userId, String oldPwd, String newPwd) {
        UserInfo user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        UserInfo update = new UserInfo();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(newPwd));
        updateById(update);
        // 修改密码后强制重新登录
        logout(userId);
    }

    /**
     * 二次确认密码验证 (不可逆操作保护)
     */
    public boolean verifyPassword(Long userId, String password) {
        UserInfo user = getById(userId);
        if (user == null || user.getDeleteFlag() == 1) {
            throw new BusinessException("用户信息异常，请重新登录");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "安全校验失败：密码错误");
        }
        return true;
    }

    // ========== 管理员操作 ==========

    /**
     * 分页查询用户列表
     */
    public Page<UserInfo> listUsers(int page, int size, String username, Integer roleType, Integer status) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getDeleteFlag, 0)
                .like(StringUtils.hasText(username), UserInfo::getUsername, username)
                .eq(roleType != null, UserInfo::getRoleType, roleType)
                .eq(status != null, UserInfo::getStatus, status)
                .orderByDesc(UserInfo::getCreateTime);

        Page<UserInfo> result = page(new Page<>(page, size), wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    /**
     * 管理员新增用户
     */
    public UserInfo addUser(UserInfo user, Long createBy) {
        // 检查用户名唯一
        long count = lambdaQuery().eq(UserInfo::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        user.setDeleteFlag(0);
        user.setCreateBy(createBy);
        save(user);
        user.setPassword(null);
        return user;
    }

    /**
     * 管理员编辑用户
     */
    public void adminUpdateUser(UserInfo user) {
        UserInfo existing = getById(user.getUserId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfo update = new UserInfo();
        update.setUserId(user.getUserId());
        update.setPhone(user.getPhone());
        update.setEmail(user.getEmail());
        update.setStatus(user.getStatus());
        updateById(update);
    }

    /**
     * 管理员删除用户（逻辑删除）
     */
    public void deleteUser(Long userId) {
        UserInfo user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRoleType() == 1) {
            throw new BusinessException("不能删除管理员账号");
        }
        removeById(userId);
        // 踢下线
        logout(userId);
    }

    /**
     * 管理员重置用户密码
     */
    public void resetPassword(Long userId) {
        UserInfo user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfo update = new UserInfo();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode("123456")); // 默认重置密码
        updateById(update);
        logout(userId);
    }
}
