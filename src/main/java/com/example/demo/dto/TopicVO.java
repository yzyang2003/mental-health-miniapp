package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 树洞帖子返回对象。
 */
@Data
public class TopicVO {

    private Long id;

    private String content;

    private List<String> images;

    private Boolean anonymous;

    /**
     * 发布者展示名称。
     */
    private String publisherName;

    private Boolean canDelete;

    private Long replyCount;

    private LocalDateTime createTime;
}
