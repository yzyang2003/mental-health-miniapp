package com.example.demo.dto.admin;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Dashboard 统计数据 VO。
 */
@Data
public class DashboardStatsVO {
    private Long totalUsers;
    private Long todayNew;
    private Long totalArticles;
    private Long totalTopics;
    private Long totalQuizCount;
    private Long totalChatMessages;
    private List<TrendItem> trendData;
    private Map<String, Long> emotionDistribution;
    private List<TrendItem> userTrend;
    private List<TrendItem> dailyActive;
    private Map<String, Long> quizByType;

    @Data
    public static class TrendItem {
        private String date;
        private Long count;
    }
}
