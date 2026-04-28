package com.example.demo.dto;

import lombok.Data;

/**
 * 当前登录用户资料更新请求。
 */
@Data
public class UserProfileUpdateRequest {

    /**
     * 昵称。
     */
    private String nickName;

    /**
     * 头像地址。
     */
    private String avatarUrl;

    /**
     * 省份。
     */
    private String province;

    /**
     * 城市。
     */
    private String city;

    /**
     * 国家。
     */
    private String country;

    /**
     * 性别：0 未知，1 男，2 女。
     */
    private Integer gender;

    /**
     * 语言。
     */
    private String language;
}
