package com.example.demo.controller;

import com.example.demo.dto.WxLoginRequest;
import com.example.demo.dto.WxLoginResponse;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import com.example.demo.utils.WxHttpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 登录控制器，负责微信小程序登录。
 */
@Tag(name = "Login", description = "WeChat mini-program login APIs")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final WxHttpUtil wxHttpUtil;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "微信小程序登录")
    @PostMapping("/login")
    public WxLoginResponse login(@RequestBody WxLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code 不能为空");
        }

        try {
            String openid = wxHttpUtil.getOpenid(request.getCode());
            User user = userService.createOrUpdateUser(openid, null);

            WxLoginResponse response = new WxLoginResponse();
            response.setToken(jwtUtil.generateToken(openid));
            response.setOpenid(openid);
            if (user != null) {
                response.setNickName(user.getNickName());
                response.setAvatarUrl(user.getAvatarUrl());
            }
            return response;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, ex.getMessage(), ex);
        }
    }
}
