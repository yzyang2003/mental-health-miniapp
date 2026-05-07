package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 音乐疗愈实体。
 */
@Data
@TableName("music_playlist")
public class Music {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("cover_image")
    private String coverImage;

    @TableField("share_url")
    private String shareUrl;

    @TableField("emotion_type")
    private String emotionType;

    @TableField("duration_text")
    private String durationText;

    @TableField("play_count")
    private Integer playCount;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
