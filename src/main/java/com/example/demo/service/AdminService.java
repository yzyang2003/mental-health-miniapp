package com.example.demo.service;

import com.example.demo.entity.Admin;

/**
 * 管理员服务接口。
 */
public interface AdminService {

    /**
     * 管理员登录。
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 管理员信息（登录成功时）
     * @throws com.example.demo.exception.BusinessException 登录失败时
     */
    Admin login(String username, String password);

    /**
     * 根据 ID 获取管理员信息。
     *
     * @param id 管理员 ID
     * @return 管理员信息
     */
    Admin getAdminById(Long id);

    /**
     * 根据用户名获取管理员信息。
     *
     * @param username 用户名
     * @return 管理员信息
     */
    Admin getAdminByUsername(String username);

    /**
     * 更新最后登录时间。
     *
     * @param id 管理员 ID
     */
    void updateLastLogin(Long id);
}
