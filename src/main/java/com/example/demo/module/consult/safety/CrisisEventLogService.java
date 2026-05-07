package com.example.demo.module.consult.safety;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 危机事件日志服务。
 * <p>
 * 记录危机事件用于追溯和统计，支持：
 * <ul>
 *   <li>危机事件记录</li>
 *   <li>危机事件统计</li>
 *   <li>危机事件查询</li>
 * </ul>
 */
@Slf4j
@Service
public class CrisisEventLogService {

    /** 危机事件日志（内存存储，可扩展为数据库） */
    private final ConcurrentLinkedQueue<CrisisEvent> crisisEvents = new ConcurrentLinkedQueue<>();

    /** 用户危机事件计数 */
    private final ConcurrentHashMap<String, Integer> userCrisisCount = new ConcurrentHashMap<>();

    /**
     * 记录危机事件
     *
     * @param openid    用户openid
     * @param content   触发危机的内容
     * @param keyword   触发的关键词
     * @param crisisLevel 危机等级
     */
    public void logCrisisEvent(String openid, String content, String keyword, CrisisLevel crisisLevel) {
        CrisisEvent event = new CrisisEvent();
        event.setOpenid(openid);
        event.setContent(content);
        event.setKeyword(keyword);
        event.setCrisisLevel(crisisLevel);
        event.setTimestamp(LocalDateTime.now());
        
        crisisEvents.add(event);
        
        // 更新用户危机计数
        userCrisisCount.merge(openid, 1, Integer::sum);
        
        log.warn("危机事件记录: openid={}, keyword={}, level={}, count={}", 
                openid, keyword, crisisLevel, userCrisisCount.get(openid));
        
        // 如果用户危机次数过多，发出警告
        if (userCrisisCount.get(openid) >= 3) {
            log.error("用户{}已触发{}次危机事件，建议重点关注", openid, userCrisisCount.get(openid));
        }
    }

    /**
     * 获取用户危机事件计数
     *
     * @param openid 用户openid
     * @return 危机事件计数
     */
    public int getUserCrisisCount(String openid) {
        return userCrisisCount.getOrDefault(openid, 0);
    }

    /**
     * 获取所有危机事件
     *
     * @return 危机事件列表
     */
    public ConcurrentLinkedQueue<CrisisEvent> getAllCrisisEvents() {
        return crisisEvents;
    }

    /**
     * 危机事件内部类
     */
    public static class CrisisEvent {
        private String openid;
        private String content;
        private String keyword;
        private CrisisLevel crisisLevel;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getOpenid() { return openid; }
        public void setOpenid(String openid) { this.openid = openid; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public CrisisLevel getCrisisLevel() { return crisisLevel; }
        public void setCrisisLevel(CrisisLevel crisisLevel) { this.crisisLevel = crisisLevel; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    /**
     * 危机等级枚举
     */
    public enum CrisisLevel {
        LOW,      // 低危
        MODERATE, // 中危
        HIGH,     // 高危
        CRISIS    // 紧急
    }
}
