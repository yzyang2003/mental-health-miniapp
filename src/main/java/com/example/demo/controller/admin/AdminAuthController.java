package com.example.demo.controller.admin;

import com.example.demo.common.Result;
import com.example.demo.dto.admin.AdminLoginRequest;
import com.example.demo.dto.admin.AdminLoginResponse;
import com.example.demo.entity.Admin;
import com.example.demo.service.AdminService;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器。
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    /**
     * 管理员登录。
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        Admin admin = adminService.login(request.getUsername(), request.getPassword());

        // 生成带 admin 角色的 JWT
        String token = jwtUtil.generateToken(admin.getUsername(), JwtUtil.ADMIN_ROLE);

        // 构造响应
        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(token);

        AdminLoginResponse.AdminInfoVO adminInfo = new AdminLoginResponse.AdminInfoVO();
        adminInfo.setId(admin.getId());
        adminInfo.setUsername(admin.getUsername());
        adminInfo.setNickname(admin.getNickname());
        adminInfo.setAvatar(admin.getAvatar());
        response.setAdmin(adminInfo);

        return Result.success(response);
    }

    /**
     * 获取当前管理员信息。
     */
    @GetMapping("/info")
    public Result<AdminLoginResponse.AdminInfoVO> getInfo(HttpServletRequest request) {
        String username = (String) request.getAttribute("openid");
        Admin admin = adminService.getAdminByUsername(username);
        if (admin == null) {
            return Result.error(404, "管理员不存在");
        }

        AdminLoginResponse.AdminInfoVO adminInfo = new AdminLoginResponse.AdminInfoVO();
        adminInfo.setId(admin.getId());
        adminInfo.setUsername(admin.getUsername());
        adminInfo.setNickname(admin.getNickname());
        adminInfo.setAvatar(admin.getAvatar());

        return Result.success(adminInfo);
    }
}
