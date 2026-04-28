package com.example.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * 微信登录接口调用工具类。
 */
@Component
public class WxHttpUtil {

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wx.appid:}")
    private String appid;

    @Value("${wx.secret:}")
    private String secret;

    @Value("${wx.mock-openid:}")
    private String mockOpenid;

    /**
     * 根据前端传入的 code 换取 openid。
     * 若配置了 mock-openid，则优先返回 mock 值，便于本地联调。
     *
     * @param code 小程序登录 code
     * @return 微信 openid
     */
    public String getOpenid(String code) {
        if (StringUtils.hasText(mockOpenid)) {
            return mockOpenid;
        }
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("微信登录 code 不能为空");
        }
        if (!StringUtils.hasText(appid) || !StringUtils.hasText(secret)) {
            throw new IllegalStateException("未配置微信 appid 或 secret");
        }

        String url = UriComponentsBuilder.fromHttpUrl(WX_LOGIN_URL)
                .queryParam("appid", appid)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();

        try {
            String responseBody = restTemplate.getForObject(url, String.class);
            if (!StringUtils.hasText(responseBody)) {
                throw new IllegalStateException("微信接口返回为空");
            }

            Map<String, Object> result = objectMapper.readValue(
                    responseBody,
                    new TypeReference<Map<String, Object>>() {
                    }
            );
            Object openidValue = result.get("openid");
            if (openidValue instanceof String openid && StringUtils.hasText(openid)) {
                return openid;
            }

            throw new IllegalStateException(
                    "获取 openid 失败，微信返回: " + responseBody
            );
        } catch (Exception ex) {
            throw new IllegalStateException("调用微信登录接口失败", ex);
        }
    }
}
