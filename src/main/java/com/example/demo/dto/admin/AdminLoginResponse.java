package com.example.demo.dto.admin;

import lombok.Data;

/**
 * 管理员登录响应 DTO。
 */
@Data
public class AdminLoginResponse {
    private String token;
    private AdminInfoVO admin;

    @Data
    public static class AdminInfoVO {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
}
