package com.example.demo.module.consult.emotion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 情绪识别服务。
 * <p>
 * 基于关键词匹配识别用户情绪类型和强度：
 * <ul>
 *   <li>焦虑/紧张</li>
 *   <li>抑郁/低落</li>
 *   <li>压力/疲惫</li>
 *   <li>愤怒/烦躁</li>
 *   <li>悲伤/难过</li>
 *   <li>失眠</li>
 *   <li>注意力分散</li>
 * </ul>
 */
@Slf4j
@Service
public class EmotionRecognitionService {

    /** 情绪关键词映射 */
    private static final Map<String, List<String>> EMOTION_KEYWORDS = Map.of(
            "焦虑", List.of(
                    "焦虑", "紧张", "担心", "不安", "害怕", "恐惧",
                    "心慌", "心跳加速", "坐立不安", "忐忑", "惶恐"
            ),
            "抑郁", List.of(
                    "抑郁", "低落", "沮丧", "失落", "难过", "伤心",
                    "消沉", "悲观", "绝望", "无助", "无望", "空虚"
            ),
            "压力", List.of(
                    "压力", "疲惫", "累", "疲倦", "精疲力竭",
                    "喘不过气", "受不了", "崩溃", "撑不住", "扛不住"
            ),
            "愤怒", List.of(
                    "愤怒", "生气", "烦躁", "恼火", "气愤",
                    "暴躁", "易怒", "火大", "气死", "恼怒"
            ),
            "悲伤", List.of(
                    "悲伤", "难过", "伤心", "痛苦", "心痛",
                    "心碎", "流泪", "哭泣", "哭", "委屈"
            ),
            "失眠", List.of(
                    "失眠", "睡不着", "入睡困难", "半夜醒来",
                    "睡眠质量差", "做噩梦", "早醒", "睡眠不好"
            ),
            "注意力分散", List.of(
                    "注意力分散", "注意力不集中", "走神", "分心",
                    "无法专注", "学习效率低", "看书看不进去"
            )
    );

    /** 情绪强度关键词 */
    private static final Map<String, List<String>> EMOTION_INTENSITY_KEYWORDS = Map.of(
            "高强度", List.of(
                    "非常", "极其", "特别", "太", "真的",
                    "受不了", "崩溃", "绝望", "痛苦到极点"
            ),
            "中强度", List.of(
                    "很", "比较", "有些", "稍微"
            ),
            "低强度", List.of(
                    "一点", "有点", "偶尔", "有时", "可能"
            )
    );

    /**
     * 识别用户情绪
     *
     * @param content 用户输入内容
     * @return 情绪识别结果
     */
    public EmotionResult recognize(String content) {
        if (content == null || content.isBlank()) {
            return EmotionResult.neutral();
        }

        String lowerContent = content.toLowerCase();
        EmotionResult result = new EmotionResult();

        // 识别情绪类型
        for (Map.Entry<String, List<String>> entry : EMOTION_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerContent.contains(keyword)) {
                    result.addEmotion(entry.getKey(), keyword);
                    log.debug("识别到情绪: {} (关键词: {})", entry.getKey(), keyword);
                }
            }
        }

        // 识别情绪强度
        for (Map.Entry<String, List<String>> entry : EMOTION_INTENSITY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerContent.contains(keyword)) {
                    result.setIntensity(entry.getKey());
                    log.debug("识别到情绪强度: {} (关键词: {})", entry.getKey(), keyword);
                    break;
                }
            }
        }

        // 如果没有识别到情绪，返回中性
        if (result.getEmotions().isEmpty()) {
            return EmotionResult.neutral();
        }

        log.info("情绪识别结果: {}", result);
        return result;
    }

    /**
     * 获取情绪对应的中文描述
     *
     * @param emotionType 情绪类型
     * @return 中文描述
     */
    public String getEmotionDescription(String emotionType) {
        return switch (emotionType) {
            case "焦虑" -> "焦虑和紧张";
            case "抑郁" -> "抑郁和低落";
            case "压力" -> "压力和疲惫";
            case "愤怒" -> "愤怒和烦躁";
            case "悲伤" -> "悲伤和难过";
            case "失眠" -> "失眠困扰";
            case "注意力分散" -> "注意力不集中";
            default -> "情绪波动";
        };
    }
}
