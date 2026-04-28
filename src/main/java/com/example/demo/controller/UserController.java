package com.example.demo.controller;

import com.example.demo.dto.UserProfileUpdateRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * 用户接口，演示 JWT 鉴权后的访问方式。
 */
@Tag(name = "User", description = "User APIs")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public Object getUserInfo(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (!(openidAttr instanceof String openid)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }

        User user = userService.getByOpenid(openid);
        if (user != null) {
            return user;
        }
        return Map.of("openid", openid);
    }

    @Operation(summary = "更新当前登录用户资料")
    @PostMapping("/profile")
    public User updateUserProfile(@RequestBody UserProfileUpdateRequest request, HttpServletRequest httpServletRequest) {
        Object openidAttr = httpServletRequest.getAttribute("openid");
        if (!(openidAttr instanceof String openid)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }

        User userInfo = new User();
        if (request != null) {
            userInfo.setNickName(request.getNickName());
            userInfo.setAvatarUrl(request.getAvatarUrl());
            userInfo.setProvince(request.getProvince());
            userInfo.setCity(request.getCity());
            userInfo.setCountry(request.getCountry());
            userInfo.setGender(request.getGender());
            userInfo.setLanguage(request.getLanguage());
        }
        return userService.updateUserProfile(openid, userInfo);
    }
}
