package com.peng.zerocodeappsandbox.aop;

import com.peng.zerocodeappsandbox.annotation.AuthCheck;
import com.peng.zerocodeappsandbox.exception.BusinessException;
import com.peng.zerocodeappsandbox.exception.ErrorCode;
import com.peng.zerocodeappsandbox.model.entity.User;
import com.peng.zerocodeappsandbox.model.enums.UserRoleEnum;
import com.peng.zerocodeappsandbox.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import cn.hutool.core.util.StrUtil;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        if (StrUtil.isBlank(mustRole)) {
            return joinPoint.proceed();
        }

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求上下文缺失");
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();

        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if (mustRoleEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "权限配置错误");
        }

        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 管理员拥有全部权限，其他角色按精确匹配判断
        if (!UserRoleEnum.ADMIN.equals(userRoleEnum) && !mustRoleEnum.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
