package com.example.demo.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class OpenidUtil {

    public static String getOpenid(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (openidAttr instanceof String openid) {
            return openid;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
    }
}
