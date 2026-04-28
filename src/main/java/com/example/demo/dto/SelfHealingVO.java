package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 自我疗愈返回对象。
 */
@Data
public class SelfHealingVO {

    private Long id;

    private String title;

    private String cover;

    private String description;

    private String issueType;

    private List<SelfHealingStepItem> steps;

    private Integer duration;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
