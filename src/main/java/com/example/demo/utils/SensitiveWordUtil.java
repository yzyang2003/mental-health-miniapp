package com.example.demo.utils;

import java.util.List;

/**
 * 简单敏感词过滤工具。
 */
public final class SensitiveWordUtil {

    private static final List<String> SENSITIVE_WORDS = List.of(
            "暴力",
            "色情",
            "赌博",
            "吸毒",
            "诈骗"
    );

    private SensitiveWordUtil() {
    }

    /**
     * 判断文本是否包含敏感词。
     *
     * @param text 待检测文本
     * @return 是否命中敏感词
     */
    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalizedText = text.trim();
        return SENSITIVE_WORDS.stream().anyMatch(normalizedText::contains);
    }
}
