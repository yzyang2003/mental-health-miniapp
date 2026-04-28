package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 心理文章返回对象。
 */
@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String cover;

    private String summary;

    private String content;

    private String contentUrl;

    private List<String> tags;

    private Integer viewCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
