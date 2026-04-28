package com.example.demo.dto;

import lombok.Data;

/**
 * 微信小程序登录响应。
 */
@Data
public class WxLoginResponse {

    /**
     * 服务端签发的 JWT。
     */
    private String token;

    /**
     * 用户微信 openid。
     */
    private String openid;

    /**
     * 预留昵称字段，便于后续扩展。
     */
    private String nickName;

    /**
     * 预留头像字段，便于后续扩展。
     */
    private String avatarUrl;
}
