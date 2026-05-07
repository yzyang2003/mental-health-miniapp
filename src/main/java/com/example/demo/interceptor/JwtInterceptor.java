package com.example.demo.interceptor;

import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * JWT 认证拦截器，支持 role-based 访问控制。
 * /api/admin/** 路径需要 admin 角色。
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

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

        // 提取用户信息并存入 request
        String openid = jwtUtil.getOpenidFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);
        request.setAttribute("openid", openid);
        request.setAttribute("role", role);

        // admin 路径需要 admin 角色
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/admin/") && !JwtUtil.ADMIN_ROLE.equals(role)) {
            writeForbidden(response, "无权访问管理接口");
            return false;
        }

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

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                OBJECT_MAPPER.writeValueAsString(Map.of("code", 403, "message", message == null ? "" : message))
        );
    }
}
