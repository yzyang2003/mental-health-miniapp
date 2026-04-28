package com.example.demo.dto;

import lombok.Data;

/**
 * 微信小程序登录请求参数。
 */
@Data
public class WxLoginRequest {

    /**
     * 小程序端调用 wx.login 获取到的 code。
     */
    private String code;
}
