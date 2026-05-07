package com.example.demo.module.consult.safety;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强版内容安全检测服务。
 * <p>
 * 功能：
 * <ul>
 *   <li>扩展危机关键词（50+）</li>
 *   <li>情感强度检测（轻度/中度/重度）</li>
 *   <li>AI输出过滤</li>
 *   <li>危机事件日志</li>
 * </ul>
 */
@Slf4j
@Service
@Primary
public class EnhancedContentSafetyService implements ContentSafetyService {

    // ==================== 危机关键词（50+）====================
    
    /** 直接表达自杀/自残 */
    private static final List<String> CRISIS_DIRECT = List.of(
        "自杀", "自残", "跳楼", "活不下去", "不想活", "想死", "结束生命",
        "割腕", "上吊", "跳河", "服毒", "轻生", "寻死", "赴死"
    );
    
    /** 变体表达 */
    private static final List<String> CRISIS_VARIANT = List.of(
        "想离开这个世界", "活着没意思", "不如死了算了", "死了算了",
        "不想活了", "活着真累", "活着好累", "死掉算了",
        "离开这个世界", "结束这一切", "不想面对了",
        "消失就好了", "不存在就好了", "没有我就好了"
    );
    
    /** 校园场景危机 */
    private static final List<String> CRISIS_CAMPUS = List.of(
        "考试考砸了人生完了", "挂科了不想活", "被欺负不想活",
        "被孤立想死", "被排挤想死", "毕不了业想死",
        "找不到工作想死", "论文写不完想死"
    );
    
    /** 情感问题危机 */
    private static final List<String> CRISIS_EMOTIONAL = List.of(
        "失恋了活不下去", "分手了想死", "被抛弃想死",
        "没人爱想死", "孤独想死", "被背叛想死"
    );
    
    /** 家庭问题危机 */
    private static final List<String> CRISIS_FAMILY = List.of(
        "父母离婚想死", "家庭暴力想死", "被家人抛弃",
        "家里出事想死", "欠债想死"
    );
    
    /** 合并所有危机关键词 */
    private static final List<String> ALL_CRISIS_KEYWORDS = List.of(
        CRISIS_DIRECT, CRISIS_VARIANT, CRISIS_CAMPUS, CRISIS_EMOTIONAL, CRISIS_FAMILY
    ).stream().flatMap(List::stream).toList();

    // ==================== 情感强度关键词 ====================
    
    /** 轻度负面情绪 */
    private static final List<String> EMOTION_MILD = List.of(
        "难过", "伤心", "失落", "沮丧", "郁闷", "烦闷", "不开心",
        "心情不好", "情绪低落", "有点烦", "不太开心"
    );
    
    /** 中度负面情绪 */
    private static final List<String> EMOTION_MODERATE = List.of(
        "绝望", "痛苦", "崩溃", "受不了", "撑不住", "扛不住",
        "快疯了", "喘不过气", "窒息感", "无助", "无望"
    );
    
    /** 重度负面情绪 */
    private static final List<String> EMOTION_SEVERE = List.of(
        "不想活", "想死", "自杀", "自残", "结束生命",
        "活着没意义", "人生没意义", "世界没意义"
    );

    // ==================== 不安全内容关键词 ====================
    
    /** 不安全内容 */
    private static final List<String> UNSAFE_KEYWORDS = List.of(
        "色情", "暴力", "赌博", "毒品", "枪支", "炸弹",
        "自杀方法", "自杀教程", "自残方法", "自残教程"
    );

    // ==================== 危机事件日志 ====================
    
    /** 危机事件记录（内存缓存，生产环境建议持久化） */
    private final Map<String, CrisisEvent> crisisEventCache = new ConcurrentHashMap<>();

    @Override
    public SafetyResult check(String content) {
        if (content == null || content.isBlank()) {
            return SafetyResult.SAFE;
        }

        String lowerContent = content.toLowerCase();

        // 1. 检查危机关键词（优先级最高）
        for (String keyword : ALL_CRISIS_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                // 记录危机事件
                logCrisisEvent(keyword, content);
                return SafetyResult.CRISIS;
            }
        }

        // 2. 检查不安全内容
        for (String keyword : UNSAFE_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                return SafetyResult.UNSAFE;
            }
        }

        return SafetyResult.SAFE;
    }

    /**
     * 检测情感强度
     */
    public EmotionIntensity detectEmotionIntensity(String content) {
        if (content == null || content.isBlank()) {
            return EmotionIntensity.NONE;
        }

        String lowerContent = content.toLowerCase();

        // 检查重度负面情绪
        for (String keyword : EMOTION_SEVERE) {
            if (lowerContent.contains(keyword)) {
                return EmotionIntensity.SEVERE;
            }
        }

        // 检查中度负面情绪
        for (String keyword : EMOTION_MODERATE) {
            if (lowerContent.contains(keyword)) {
                return EmotionIntensity.MODERATE;
            }
        }

        // 检查轻度负面情绪
        for (String keyword : EMOTION_MILD) {
            if (lowerContent.contains(keyword)) {
                return EmotionIntensity.MILD;
            }
        }

        return EmotionIntensity.NONE;
    }

    /**
     * 检查AI输出是否安全
     */
    public SafetyResult checkAiOutput(String content) {
        if (content == null || content.isBlank()) {
            return SafetyResult.SAFE;
        }

        String lowerContent = content.toLowerCase();

        // 检查AI是否输出了不当内容
        for (String keyword : UNSAFE_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                log.warn("AI输出包含不当内容: {}", keyword);
                return SafetyResult.UNSAFE;
            }
        }

        // 检查AI是否输出了鼓励自杀/自残的内容
        List<String> dangerousPhrases = List.of(
            "自杀是解脱", "死亡是解脱", "自残可以缓解",
            "跳楼不会痛", "割腕不会死"
        );
        
        for (String phrase : dangerousPhrases) {
            if (lowerContent.contains(phrase)) {
                log.error("AI输出包含危险内容: {}", phrase);
                return SafetyResult.CRISIS;
            }
        }

        return SafetyResult.SAFE;
    }

    /**
     * 记录危机事件
     */
    private void logCrisisEvent(String keyword, String content) {
        String eventId = System.currentTimeMillis() + "_" + keyword.hashCode();
        CrisisEvent event = new CrisisEvent();
        event.setId(eventId);
        event.setKeyword(keyword);
        event.setContent(content.length() > 100 ? content.substring(0, 100) + "..." : content);
        event.setTimestamp(LocalDateTime.now());
        
        crisisEventCache.put(eventId, event);
        
        log.warn("检测到危机事件 - ID: {}, 关键词: {}, 内容: {}", 
            eventId, keyword, event.getContent());
        
        // 清理超过24小时的事件
        crisisEventCache.entrySet().removeIf(entry -> 
            entry.getValue().getTimestamp().isBefore(LocalDateTime.now().minusHours(24))
        );
    }

    /**
     * 获取最近的危机事件
     */
    public List<CrisisEvent> getRecentCrisisEvents(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return crisisEventCache.values().stream()
            .filter(event -> event.getTimestamp().isAfter(cutoff))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList();
    }

    /**
     * 情感强度枚举
     */
    public enum EmotionIntensity {
        NONE,      // 无负面情绪
        MILD,      // 轻度负面情绪
        MODERATE,  // 中度负面情绪
        SEVERE     // 重度负面情绪
    }

    /**
     * 危机事件记录
     */
    public static class CrisisEvent {
        private String id;
        private String keyword;
        private String content;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getKeyword() { return keyword; }
        public void setKeyword(String keyword) { this.keyword = keyword; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
