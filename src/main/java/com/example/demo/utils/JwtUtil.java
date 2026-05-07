package com.example.demo.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类，负责生成和校验令牌。
 * 支持 role claim 用于管理后台权限控制。
 */
@Component
public class JwtUtil {

    public static final String ROLE_KEY = "role";
    public static final String DEFAULT_ROLE = "user";
    public static final String ADMIN_ROLE = "admin";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 根据 openid 生成 JWT（兼容现有小程序登录）。
     *
     * @param openid 用户微信 openid
     * @return JWT 字符串
     */
    public String generateToken(String openid) {
        return generateToken(openid, DEFAULT_ROLE);
    }

    /**
     * 根据 openid 和 role 生成 JWT。
     *
     * @param openid 用户标识（小程序为 openid，管理后台为 admin username）
     * @param role   用户角色（user / admin）
     * @return JWT 字符串
     */
    public String generateToken(String openid, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(openid)
                .claim(ROLE_KEY, role)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 JWT 中提取 openid。
     *
     * @param token JWT 字符串
     * @return openid
     */
    public String getOpenidFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 从 JWT 中提取 role。
     * 如果 token 中没有 role claim，则返回默认角色 "user"（向后兼容）。
     *
     * @param token JWT 字符串
     * @return role（user / admin）
     */
    public String getRoleFromToken(String token) {
        try {
            Object role = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get(ROLE_KEY);
            return role != null ? role.toString() : DEFAULT_ROLE;
        } catch (JwtException | IllegalArgumentException ex) {
            return DEFAULT_ROLE;
        }
    }

    /**
     * 校验 JWT 是否有效。
     *
     * @param token JWT 字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private SecretKey getSigningKey() {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "未配置 jwt.secret，请在 application-local.yml 或环境变量 JWT_SECRET 中设置（HS256 至少 32 个字符）");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "jwt.secret 长度不足：至少需要 32 个字符，当前为 "
                            + keyBytes.length
                            + "。请修改 JWT_SECRET。");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
