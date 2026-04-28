package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 音乐疗愈返回对象。
 */
@Data
public class MusicVO {

    private Long id;

    private String songName;

    private String singer;

    private String cover;

    private String emotionType;

    private String url;

    private Integer duration;

    private Integer playCount;

    private LocalDateTime createTime;
}
