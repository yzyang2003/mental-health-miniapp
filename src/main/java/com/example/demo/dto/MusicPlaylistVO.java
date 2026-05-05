package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 音乐疗愈歌单返回对象。
 */
@Data
public class MusicPlaylistVO {

    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private String shareUrl;

    private String emotionType;

    private String durationText;

    private Integer playCount;

    private LocalDateTime createTime;
}
