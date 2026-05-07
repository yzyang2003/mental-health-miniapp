package com.example.demo.module.consult.emotion;

import java.util.ArrayList;
import java.util.List;

/**
 * 情绪识别结果。
 */
public class EmotionResult {

    /** 识别到的情绪列表 */
    private List<String> emotions = new ArrayList<>();

    /** 触发情绪的关键词 */
    private List<String> keywords = new ArrayList<>();

    /** 情绪强度：高强度/中强度/低强度 */
    private String intensity = "中强度";

    /** 是否为中性情绪 */
    private boolean neutral = false;

    /**
     * 添加情绪
     *
     * @param emotion 情绪类型
     * @param keyword 触发关键词
     */
    public void addEmotion(String emotion, String keyword) {
        if (!emotions.contains(emotion)) {
            emotions.add(emotion);
        }
        if (!keywords.contains(keyword)) {
            keywords.add(keyword);
        }
    }

    /**
     * 创建中性情绪结果
     *
     * @return 中性情绪结果
     */
    public static EmotionResult neutral() {
        EmotionResult result = new EmotionResult();
        result.setNeutral(true);
        return result;
    }

    /**
     * 获取主要情绪（第一个识别到的情绪）
     *
     * @return 主要情绪类型，如果没有识别到情绪则返回null
     */
    public String getPrimaryEmotion() {
        return emotions.isEmpty() ? null : emotions.get(0);
    }

    /**
     * 是否包含特定情绪
     *
     * @param emotion 情绪类型
     * @return 是否包含
     */
    public boolean hasEmotion(String emotion) {
        return emotions.contains(emotion);
    }

    // Getters and Setters
    public List<String> getEmotions() { return emotions; }
    public void setEmotions(List<String> emotions) { this.emotions = emotions; }
    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
    public String getIntensity() { return intensity; }
    public void setIntensity(String intensity) { this.intensity = intensity; }
    public boolean isNeutral() { return neutral; }
    public void setNeutral(boolean neutral) { this.neutral = neutral; }

    @Override
    public String toString() {
        return "EmotionResult{" +
                "emotions=" + emotions +
                ", keywords=" + keywords +
                ", intensity='" + intensity + '\'' +
                ", neutral=" + neutral +
                '}';
    }
}
