package com.example.demo.dto.admin;

import lombok.Data;

/**
 * 管理员登录请求 DTO。
 */
@Data
public class AdminLoginRequest {
    private String username;
    private String password;
}
