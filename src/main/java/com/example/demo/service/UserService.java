package com.example.demo.service;

import com.example.demo.entity.User;

/**
 * 用户服务接口。
 */
public interface UserService {

    /**
     * 按 openid 查询用户。
     *
     * @param openid 微信 openid
     * @return 用户信息
     */
    User getByOpenid(String openid);

    /**
     * 创建用户或更新已有用户资料。
     *
     * @param openid   微信 openid
     * @param userInfo 用户补充信息，可为空
     * @return 保存后的用户信息
     */
    User createOrUpdateUser(String openid, User userInfo);

    /**
     * 更新指定 openid 对应的用户资料。
     *
     * @param openid   微信 openid
     * @param userInfo 用户资料
     * @return 更新后的用户信息
     */
    User updateUserProfile(String openid, User userInfo);

    /**
     * 根据 openid 获取用户昵称。
     *
     * @param openid 微信 openid
     * @return 用户昵称，不存在时返回空字符串
     */
    String getNicknameByOpenid(String openid);
}
