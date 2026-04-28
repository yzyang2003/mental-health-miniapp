package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应微信小程序登录用户表。
 */
@Data
@TableName("`user`")
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("openid")
    private String openid;

    @TableField("nick_name")
    private String nickName;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("province")
    private String province;

    @TableField("city")
    private String city;

    @TableField("country")
    private String country;

    @TableField("gender")
    private Integer gender;

    @TableField("language")
    private String language;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
