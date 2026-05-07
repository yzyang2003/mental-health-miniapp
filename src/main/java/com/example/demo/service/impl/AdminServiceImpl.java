package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.Admin;
import com.example.demo.mapper.AdminMapper;
import com.example.demo.service.AdminService;
import com.example.demo.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Admin login(String username, String password) {
        Admin admin = getAdminByUsername(username);
        if (admin == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (admin.getStatus() != null && admin.getStatus() != 1) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        // 更新最后登录时间
        updateLastLogin(admin.getId());
        // 清除密码哈希后返回
        admin.setPasswordHash(null);
        return admin;
    }

    @Override
    public Admin getAdminById(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) {
            admin.setPasswordHash(null); // 不返回密码哈希
        }
        return admin;
    }

    @Override
    public Admin getAdminByUsername(String username) {
        return adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username)
                        .last("limit 1")
        );
    }

    @Override
    public void updateLastLogin(Long id) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setLastLogin(LocalDateTime.now());
        adminMapper.updateById(admin);
    }
}
