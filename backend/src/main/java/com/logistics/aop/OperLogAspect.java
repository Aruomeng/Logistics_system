package com.logistics.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.entity.OperationLog;
import com.logistics.mapper.OperationLogMapper;
import com.logistics.security.LoginUser;
import com.logistics.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperationLogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        OperationLog opLog = new OperationLog();

        // 请求信息
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            opLog.setRequestUrl(request.getRequestURI());
            opLog.setOperatorIp(request.getRemoteAddr());
        }

        // 操作人信息
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            LoginUser loginUser = SecurityUtil.getCurrentUser();
            if (userId != null) {
                opLog.setOperatorId(userId);
                opLog.setOperatorName(loginUser.getUsername());
                opLog.setOperatorRole(loginUser.getRoleType());
            }
        } catch (Exception ignored) {
        }

        opLog.setModule(operLog.module());
        opLog.setOperationType(operLog.type());
        opLog.setDescription(operLog.desc());

        MethodSignature sig = (MethodSignature) point.getSignature();
        opLog.setMethod(sig.getDeclaringTypeName() + "." + sig.getName());

        // 请求参数
        try {
            opLog.setRequestParams(objectMapper.writeValueAsString(point.getArgs()));
        } catch (Exception ignored) {
        }

        Object result;
        try {
            result = point.proceed();
            opLog.setStatus(1);
        } catch (Throwable e) {
            opLog.setStatus(0);
            opLog.setResponseResult(e.getMessage());
            throw e;
        } finally {
            opLog.setCostTime(System.currentTimeMillis() - start);
            try {
                logMapper.insert(opLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
        }
        return result;
    }
}
