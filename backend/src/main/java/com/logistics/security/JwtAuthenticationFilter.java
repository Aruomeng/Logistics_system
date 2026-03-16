package com.logistics.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.JwtUtil;
import com.logistics.common.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 认证过滤器 - 每次请求前校验 Token
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("null")
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            Integer roleType = jwtUtil.getRoleType(token);

            // 校验 Redis 中 Token 是否存在（支持踢下线）
            String redisKey = "user:token:" + userId;
            Object storedToken = redisTemplate.opsForValue().get(redisKey);
            if (storedToken == null || !storedToken.toString().equals(token)) {
                writeError(response, 401, "令牌已失效，请重新登录");
                return;
            }

            // 构建认证信息，角色转为 ROLE_ 前缀
            String roleName = switch (roleType) {
                case 1 -> "ROLE_ADMIN";
                case 2 -> "ROLE_SUPPLIER";
                case 3 -> "ROLE_LOGISTICS";
                case 4 -> "ROLE_CONSUMER";
                default -> "ROLE_UNKNOWN";
            };

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null,
                    List.of(new SimpleGrantedAuthority(roleName)));
            // 把用户信息放入 details 方便后续获取
            authentication.setDetails(new LoginUser(userId, username, roleType));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 优先从 Authorization Header 取
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        // 其次从 URL 参数取（用于文件下载等无法携带 Header 的场景）
        String paramToken = request.getParameter("token");
        if (StringUtils.hasText(paramToken)) {
            return paramToken;
        }
        return null;
    }

    private void writeError(HttpServletResponse response, int code, String msg) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, msg)));
    }
}
