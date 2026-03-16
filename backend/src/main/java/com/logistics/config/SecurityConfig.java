package com.logistics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.Result;
import com.logistics.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final ObjectMapper objectMapper;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // 关闭 CSRF（JWT 无状态不需要）
                                .csrf(AbstractHttpConfigurer::disable)
                                // 无状态 Session
                                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // 放行规则
                                .authorizeHttpRequests(auth -> auth
                                                // 登录、密码找回、验证码（公开）
                                                .requestMatchers("/user/login", "/user/password/retrieve",
                                                                "/user/password/reset", "/captcha/**")
                                                .permitAll()
                                                // 公开供应信息查询、溯源查询
                                                .requestMatchers("/supply/common/**", "/trace/public/**").permitAll()
                                                // 公告公开查询
                                                .requestMatchers("/notice/common/**").permitAll()
                                                // 需求公开查询
                                                .requestMatchers("/demand/common/**").permitAll()
                                                // Swagger 文档
                                                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()
                                                // 文件访问
                                                .requestMatchers("/files/**").permitAll()
                                                // OPTIONS 预检请求
                                                .requestMatchers(HttpMethod.OPTIONS).permitAll()
                                                // 管理员接口
                                                .requestMatchers("/user/admin/**", "/notice/admin/**",
                                                                "/supply/admin/**",
                                                                "/order/admin/**", "/log/**", "/export/**",
                                                                "/demand/admin/**")
                                                .hasRole("ADMIN")
                                                // 供应方接口
                                                .requestMatchers("/supply/add", "/supply/update", "/supply/list",
                                                                "/supply/online", "/supply/offline",
                                                                "/order/supplier/**",
                                                                "/demand/accept/add")
                                                .hasRole("SUPPLIER")
                                                // 物流方接口
                                                .requestMatchers("/order/logistics/**").hasRole("LOGISTICS")
                                                // 消费者接口
                                                .requestMatchers("/order/create", "/order/consumer/**",
                                                                "/demand/add", "/demand/update",
                                                                "/demand/my/**", "/demand/online",
                                                                "/demand/offline", "/demand/close",
                                                                "/demand/accept/confirm", "/demand/accept/reject")
                                                .hasRole("CONSUMER")
                                                // 其余全部需要认证
                                                .anyRequest().authenticated())
                                // 异常处理
                                .exceptionHandling(ex -> ex
                                                // 未认证
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(401);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write(
                                                                        objectMapper.writeValueAsString(
                                                                                        Result.fail(401, "未登录或令牌已过期")));
                                                })
                                                // 无权限
                                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                                        response.setStatus(403);
                                                        response.setContentType("application/json;charset=UTF-8");
                                                        response.getWriter().write(
                                                                        objectMapper.writeValueAsString(
                                                                                        Result.fail(403, "权限不足，拒绝访问")));
                                                }))
                                // JWT 过滤器在 UsernamePasswordAuthenticationFilter 之前
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
