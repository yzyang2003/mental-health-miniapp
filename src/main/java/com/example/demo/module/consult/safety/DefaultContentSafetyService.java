package com.example.demo.module.consult.safety;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 默认内容安全检测服务实现。
 * <p>
 * 基于关键词匹配进行内容安全分级：
 * <ul>
 *   <li>{@link SafetyResult#CRISIS} - 危机关键词，需要紧急响应</li>
 *   <li>{@link SafetyResult#UNSAFE} - 不安全内容，拒绝处理</li>
 *   <li>{@link SafetyResult#SAFE} - 安全内容，正常处理</li>
 * </ul>
 */
@Service
public class DefaultContentSafetyService implements ContentSafetyService {

    /** 危机关键词 - 需要紧急响应 */
    private static final List<String> CRISIS_KEYWORDS = List.of(
            "自杀", "自残", "跳楼", "活不下去", "不想活", "想死", "结束生命",
            "割腕", "上吊", "跳河", "服毒", "轻生"
    );

    /** 不安全内容关键词 - 拒绝处理 */
    private static final List<String> UNSAFE_KEYWORDS = List.of(
            "色情", "暴力", "赌博", "毒品", "枪支", "炸弹"
    );

    @Override
    public SafetyResult check(String content) {
        if (content == null || content.isBlank()) {
            return SafetyResult.SAFE;
        }

        String lowerContent = content.toLowerCase();

        // 检查危机关键词（优先级最高）
        for (String keyword : CRISIS_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                return SafetyResult.CRISIS;
            }
        }

        // 检查不安全内容
        for (String keyword : UNSAFE_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                return SafetyResult.UNSAFE;
            }
        }

        return SafetyResult.SAFE;
    }
}
