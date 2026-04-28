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
@TableName("music")
public class Music {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("song_name")
    private String songName;

    @TableField("singer")
    private String singer;

    @TableField("cover")
    private String cover;

    @TableField("emotion_type")
    private String emotionType;

    @TableField("url")
    private String url;

    @TableField("duration")
    private Integer duration;

    @TableField("play_count")
    private Integer playCount;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
