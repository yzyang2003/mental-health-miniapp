package com.example.demo.interceptor;

import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * JWT 认证拦截器。
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少或非法的 Authorization 头");
            return false;
        }

        String token = authorization.substring(7);
        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "token 无效或已过期");
            return false;
        }

        String openid = jwtUtil.getOpenidFromToken(token);
        request.setAttribute("openid", openid);
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                OBJECT_MAPPER.writeValueAsString(Map.of("code", 401, "message", message == null ? "" : message))
        );
    }
}
