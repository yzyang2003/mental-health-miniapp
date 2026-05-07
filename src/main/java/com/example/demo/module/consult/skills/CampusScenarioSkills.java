package com.example.demo.module.consult.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 校园场景技能层。
 * <p>
 * 提供校园心理咨询的场景化技能：
 * <ul>
 *   <li>学业压力</li>
 *   <li>人际关系</li>
 *   <li>情感问题</li>
 *   <li>职业规划</li>
 * </ul>
 */
@Slf4j
@Service
public class CampusScenarioSkills {

    /** 场景关键词映射 */
    private static final Map<String, List<String>> SCENARIO_KEYWORDS = Map.of(
            "学业压力", List.of(
                    "考试", "挂科", "成绩", "学习", "作业", "论文",
                    "毕业", "考研", "考公", "期末", "期中", "GPA"
            ),
            "人际关系", List.of(
                    "室友", "同学", "朋友", "关系", "矛盾", "冲突",
                    "被欺负", "被孤立", "被排挤", "社交", "沟通"
            ),
            "情感问题", List.of(
                    "恋爱", "失恋", "分手", "暗恋", "表白", "感情",
                    "男朋友", "女朋友", "对象", "喜欢", "爱"
            ),
            "职业规划", List.of(
                    "工作", "实习", "就业", "职业", "前途", "未来",
                    "迷茫", "方向", "选择", "规划", "简历", "面试"
            )
    );

    /** 场景响应模板 */
    private static final Map<String, List<String>> SCENARIO_RESPONSES = Map.of(
            "学业压力", List.of(
                    "学业压力确实很大。我们可以一起制定一个学习计划，或者尝试一些放松技巧。",
                    "考试和成绩确实会让人焦虑。你愿意和我聊聊具体是哪方面的压力吗？",
                    "学习压力是很多同学都会遇到的。我们可以一起想想怎么缓解。"
            ),
            "人际关系", List.of(
                    "人际关系的困扰很常见。我们可以聊聊具体发生了什么，一起寻找解决方法。",
                    "和同学相处确实需要一些技巧。你愿意和我分享一下具体情况吗？",
                    "社交方面的困扰确实会让人感到孤独。我们可以一起想想怎么改善。"
            ),
            "情感问题", List.of(
                    "感情上的痛苦是真实的。让我们先照顾好自己的情绪。",
                    "恋爱中的困扰确实会让人感到痛苦。你愿意和我聊聊吗？",
                    "感情问题是很多同学都会遇到的。我们可以一起面对。"
            ),
            "职业规划", List.of(
                    "职业迷茫是正常的。我们可以一起探索你的兴趣和优势。",
                    "未来的选择确实会让人感到困惑。你愿意和我聊聊你的想法吗？",
                    "职业规划是很多同学都会思考的问题。我们可以一起想想。"
            )
    );

    /**
     * 识别场景类型
     *
     * @param content 用户输入内容
     * @return 场景类型，如果没有识别到则返回null
     */
    public String identifyScenario(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }

        String lowerContent = content.toLowerCase();

        for (Map.Entry<String, List<String>> entry : SCENARIO_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lowerContent.contains(keyword)) {
                    log.debug("识别到场景: {} (关键词: {})", entry.getKey(), keyword);
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    /**
     * 生成场景响应
     *
     * @param scenarioType 场景类型
     * @return 场景响应
     */
    public String generateScenarioResponse(String scenarioType) {
        if (scenarioType == null) {
            return null;
        }

        List<String> responses = SCENARIO_RESPONSES.get(scenarioType);
        if (responses == null || responses.isEmpty()) {
            return null;
        }

        // 随机选择一个响应
        int index = (int) (Math.random() * responses.size());
        return responses.get(index);
    }
}
