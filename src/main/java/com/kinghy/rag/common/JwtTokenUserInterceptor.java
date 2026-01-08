package com.kinghy.rag.common;

import com.kinghy.rag.constant.JwtClaimsConstant;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.config.JwtProperties;
import com.kinghy.rag.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理CORS预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getUserTokenName());
        
        // 如果按配置的token名称获取不到，尝试从Authorization头获取
        if (token == null || token.trim().isEmpty()) {
            token = request.getHeader("Authorization");
        }

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去除 "Bearer " 前缀
        }

        // 检查token是否为空
        if (token == null || token.trim().isEmpty()) {
            log.warn("JWT token为空，请求路径: {}", request.getRequestURI());
            response.setStatus(401);
            return false;
        }

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            
            // 安全地获取userId
            Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
            if (userIdObj == null) {
                log.error("JWT中userId为null");
                response.setStatus(401);
                return false;
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            log.info("当前用户的id：{}", userId);
            BaseContext.setCurrentId(userId);
            //3、通过，放行
            return true;
        } catch (ExpiredJwtException ex) {
            log.error("令牌已过期", ex);
            response.setStatus(401);
            return false;
        } catch (Exception ex) {
            log.error("校验jwt异常", ex);
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，避免内存泄漏
        BaseContext.removeCurrentId();
    }
}
