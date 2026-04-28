package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final Map<String, String> nicknameCache = new ConcurrentHashMap<>();

    @Override
    public User getByOpenid(String openid) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getOpenid, openid)
                        .last("limit 1")
        );
    }

    @Override
    public User createOrUpdateUser(String openid, User userInfo) {
        User existingUser = getByOpenid(openid);
        if (existingUser == null) {
            User newUser = new User();
            newUser.setOpenid(openid);
            mergeUserInfo(newUser, userInfo);
            userMapper.insert(newUser);
            nicknameCache.remove(openid);
            return getByOpenid(openid);
        }

        mergeUserInfo(existingUser, userInfo);
        userMapper.updateById(existingUser);
        nicknameCache.remove(openid);
        return existingUser;
    }

    @Override
    public User updateUserProfile(String openid, User userInfo) {
        return createOrUpdateUser(openid, userInfo);
    }

    @Override
    public String getNicknameByOpenid(String openid) {
        if (!StringUtils.hasText(openid)) {
            return "";
        }
        if (nicknameCache.containsKey(openid)) {
            return nicknameCache.get(openid);
        }
        User user = getByOpenid(openid);
        if (user == null || !StringUtils.hasText(user.getNickName())) {
            nicknameCache.put(openid, "");
            return "";
        }
        String nickname = user.getNickName();
        nicknameCache.put(openid, nickname);
        return nickname;
    }

    /**
     * 仅覆盖前端明确传入的用户资料，避免用空值覆盖已有数据。
     */
    private void mergeUserInfo(User target, User source) {
        if (source == null) {
            return;
        }

        if (StringUtils.hasText(source.getNickName())) {
            target.setNickName(source.getNickName());
        }
        if (StringUtils.hasText(source.getAvatarUrl())) {
            target.setAvatarUrl(source.getAvatarUrl());
        }
        if (StringUtils.hasText(source.getProvince())) {
            target.setProvince(source.getProvince());
        }
        if (StringUtils.hasText(source.getCity())) {
            target.setCity(source.getCity());
        }
        if (StringUtils.hasText(source.getCountry())) {
            target.setCountry(source.getCountry());
        }
        if (source.getGender() != null) {
            target.setGender(source.getGender());
        }
        if (StringUtils.hasText(source.getLanguage())) {
            target.setLanguage(source.getLanguage());
        }
    }
}
