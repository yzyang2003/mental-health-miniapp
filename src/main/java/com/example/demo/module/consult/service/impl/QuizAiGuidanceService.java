package com.example.demo.module.consult.service.impl;

import com.example.demo.module.consult.client.OpenAiCompletionClient;
import com.example.demo.module.consult.dto.Scl90FactorVO;
import com.example.demo.module.consult.dto.Scl90ReportVO;
import com.example.demo.module.consult.entity.QuizResult;
import com.example.demo.module.consult.mapper.QuizResultMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * 测评 AI 指导生成服务。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuizAiGuidanceService {

    private static final int MAX_AI_GUIDANCE_CHARS = 8000;
    private static final String TYPE_SCL90_DEMO = "SCL90_DEMO";
    private static final int SCL90_AI_MAX_FACTORS = 10;
    private static final ExecutorService QUIZ_AI_EXECUTOR = Executors.newFixedThreadPool(4);

    private static final String QUIZ_AI_SYSTEM_PROMPT = "你是校园心理陪伴助手\u300c小爱\u300d的测评报告分支。根据用户提供的结构化测评摘要，输出两段内容（使用简体中文）：\n" +
            "1）第一行标题为\u300c解读摘要\u300d，换行后写 2\uff5e4 句支持性、非诊断性总结，避免医学诊断与绝对化表述。\n" +
            "2）空一行后写标题\u300c行动建议\u300d，换行后给出 3\uff5e5 条具体、可执行的自助建议（每条一行，可用\u300c\u00b7\u300d或数字开头）。\n" +
            "明确说明内容仅供自我调节参考，不可替代专业诊疗。语气温柔、简洁。";

    private final OpenAiCompletionClient openAiCompletionClient;
    private final QuizResultMapper quizResultMapper;

    @PreDestroy
    void shutdownQuizAiExecutor() {
        QUIZ_AI_EXECUTOR.shutdown();
        try {
            if (!QUIZ_AI_EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                QUIZ_AI_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            QUIZ_AI_EXECUTOR.shutdownNow();
        }
    }

    /**
     * 单次调用大模型生成测评指导；任何失败返回 {@code null}，不阻塞提交。
     */
    public String requestAiGuidanceSafe(
            String questionnaireTitle,
            String questionnaireType,
            int score,
            int maxScore,
            int scorePercent,
            String conclusion,
            List<String> ruleSuggestionLines,
            Scl90ReportVO scl90Report
    ) {
        try {
            List<Map<String, String>> messages;
            if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
                String stage1Prompt = buildScl90AiUserPrompt(scl90Report);
                messages = List.of(
                        Map.of("role", "system", "content", "你是一位具有临床心理评估经验的 AI 助手，请严格依据用户提供的量表数据与常模输出结构化报告。"),
                        Map.of("role", "user", "content", stage1Prompt)
                );
                log.info("SCL90 AI stage1 prompt prepared, length={}", stage1Prompt.length());
            } else {
                String userContent = buildQuizAiUserPrompt(
                        questionnaireTitle,
                        questionnaireType,
                        score,
                        maxScore,
                        scorePercent,
                        conclusion,
                        ruleSuggestionLines
                );
                messages = List.of(
                        Map.of("role", "system", "content", QUIZ_AI_SYSTEM_PROMPT.trim()),
                        Map.of("role", "user", "content", userContent)
                );
            }
            String raw = openAiCompletionClient.complete(messages);
            if (!StringUtils.hasText(raw)) {
                log.warn("Quiz AI guidance empty content, type={}", questionnaireType);
                if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
                    return buildScl90FallbackGuidance(scl90Report);
                }
                return null;
            }
            if (raw.length() > MAX_AI_GUIDANCE_CHARS) {
                return raw.substring(0, MAX_AI_GUIDANCE_CHARS).trim();
            }
            return raw.trim();
        } catch (Exception ex) {
            log.warn("Quiz AI guidance generation failed", ex);
            if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
                return buildScl90FallbackGuidance(scl90Report);
            }
            return null;
        }
    }

    public String buildLocalGuidance(String questionnaireType, String conclusion, List<String> suggestionLines, Scl90ReportVO scl90Report) {
        if (TYPE_SCL90_DEMO.equalsIgnoreCase(questionnaireType) && scl90Report != null) {
            return buildScl90FallbackGuidance(scl90Report);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("本报告基于量表数据自动生成，仅供自我了解参考，不能替代专业医疗诊断。\n\n");
        sb.append("当前结果提示为\u300c").append(conclusion).append("\u300d。\n");
        if (suggestionLines != null && !suggestionLines.isEmpty()) {
            sb.append("建议你优先尝试以下做法：\n");
            for (int i = 0; i < suggestionLines.size(); i++) {
                sb.append(i + 1).append(". ").append(suggestionLines.get(i)).append("\n");
            }
        } else {
            sb.append("建议先保持规律作息、适度运动，并在情绪波动持续时及时寻求专业支持。\n");
        }
        sb.append("\n系统会在后台继续生成更个性化的 AI 指导，稍后将自动更新。");
        return sb.toString().trim();
    }

    public void triggerAsyncAiGuidanceGeneration(
            Long resultId,
            String questionnaireTitle,
            String questionnaireType,
            int score,
            int maxScore,
            int scorePercent,
            String conclusion,
            List<String> suggestionLines,
            Scl90ReportVO scl90Report
    ) {
        if (resultId == null) {
            return;
        }
        QUIZ_AI_EXECUTOR.submit(() -> {
            try {
                String aiGuidance = requestAiGuidanceSafe(
                        questionnaireTitle,
                        questionnaireType,
                        score,
                        maxScore,
                        scorePercent,
                        conclusion,
                        suggestionLines,
                        scl90Report
                );
                if (!StringUtils.hasText(aiGuidance)) {
                    return;
                }
                QuizResult update = new QuizResult();
                update.setId(resultId);
                update.setAiGuidance(aiGuidance);
                quizResultMapper.updateById(update);
            } catch (Exception ex) {
                log.warn("Async quiz AI guidance generation failed for resultId={}", resultId, ex);
            }
        });
    }

    private static String buildQuizAiUserPrompt(
            String questionnaireTitle,
            String questionnaireType,
            int score,
            int maxScore,
            int scorePercent,
            String conclusion,
            List<String> ruleSuggestionLines
    ) {
        String title = StringUtils.hasText(questionnaireTitle) ? questionnaireTitle : "心理测评";
        String type = StringUtils.hasText(questionnaireType) ? questionnaireType : "未标注";
        StringBuilder sb = new StringBuilder();
        sb.append("问卷：").append(title).append("\n");
        sb.append("类型标识：").append(type).append("\n");
        sb.append("得分：").append(score).append(" / ").append(maxScore).append("\n");
        sb.append("得分占比：").append(scorePercent).append("%\n");
        sb.append("系统规则结论档：").append(conclusion).append("\n");
        sb.append("系统规则建议条目：\n");
        if (ruleSuggestionLines != null && !ruleSuggestionLines.isEmpty()) {
            IntStream.range(0, ruleSuggestionLines.size()).forEach(i ->
                    sb.append(i + 1).append(". ").append(ruleSuggestionLines.get(i)).append("\n")
            );
        } else {
            sb.append("（无）\n");
        }
        sb.append("请基于以上摘要生成\u300c解读摘要\u300d与\u300c行动建议\u300d，不要重复逐条照抄规则建议，应结合整体得分做个性化表述。");
        return sb.toString();
    }

    private static String buildScl90AiUserPrompt(Scl90ReportVO report) {
        StringBuilder sb = new StringBuilder();
        sb.append("请基于下列 SCL-90 数据输出结构化专业报告（第一阶段草稿），控制在 900 字内，强调关键风险与建议。\n\n");
        sb.append("【常模（N=1388）】\n");
        sb.append("- 躯体化：1.37\u00b10.48\n");
        sb.append("- 强迫症状：1.62\u00b10.58\n");
        sb.append("- 人际敏感：1.65\u00b10.61\n");
        sb.append("- 抑郁：1.50\u00b10.59\n");
        sb.append("- 焦虑：1.39\u00b10.43\n");
        sb.append("- 敌对：1.46\u00b10.55\n");
        sb.append("- 恐怖：1.23\u00b10.41\n");
        sb.append("- 偏执：1.43\u00b10.57\n");
        sb.append("- 精神病性：1.29\u00b10.42\n\n");
        sb.append("【本次评估】\n");
        sb.append("- 总分：【").append(report.getTotalScore()).append("】\n");
        sb.append("- 总症状指数：【").append(report.getTotalAvg()).append("】\n");
        sb.append("- 阳性项目数：【").append(report.getPositiveCount()).append("】\n");
        List<Scl90FactorVO> factors = report.getFactors() == null ? Collections.emptyList() : new ArrayList<>(report.getFactors());
        factors.sort(Comparator.comparing(Scl90FactorVO::getAvgScore, Comparator.nullsLast(Double::compareTo)).reversed());
        if (factors.size() > SCL90_AI_MAX_FACTORS) {
            factors = factors.subList(0, SCL90_AI_MAX_FACTORS);
        }
        for (Scl90FactorVO factor : factors) {
            sb.append("- ").append(factor.getName()).append("：均分").append(factor.getAvgScore())
                    .append("，总分").append(factor.getTotalScore()).append("\n");
        }
        sb.append("\n【生成规则】\n");
        sb.append("1) 开头必须写免责声明：\u201c本报告基于量表数据自动生成，仅供自我了解参考，不能替代专业医疗诊断。\u201d\n");
        sb.append("2) 严重程度分级：\n");
        sb.append("   - 无症状：均分 < 常模 M+SD\n");
        sb.append("   - 轻度：均分介于 M+SD 至 M+2SD 之间，或 2.0~2.9 区间\n");
        sb.append("   - 中度及以上：均分 \u2265 3.0 或超过 M+2SD\n");
        sb.append("3) 各维度必须按均分降序分析。\n");
        sb.append("4) 必须体现\u201c抑郁-焦虑-躯体化\u201d与\u201c强迫-焦虑\u201d关联。\n");
        sb.append("5) \u201c其它\u201d因子需单独解读睡眠/饮食并关联抑郁或焦虑。\n\n");
        sb.append("【输出结构】\n");
        sb.append("## 一、总体筛查结果简报\n");
        sb.append("## 二、各维度症状详细解读（降序）\n");
        sb.append("## 三、综合分析与生活建议\n");
        sb.append("## 四、专业求助指引\n");
        sb.append("\n请直接输出报告正文。");
        return sb.toString();
    }

    private static String buildScl90FallbackGuidance(Scl90ReportVO report) {
        List<Scl90FactorVO> factors = report.getFactors() == null ? Collections.emptyList() : new ArrayList<>(report.getFactors());
        factors.sort(Comparator.comparing(Scl90FactorVO::getAvgScore, Comparator.nullsLast(Double::compareTo)).reversed());
        String top1 = factors.isEmpty() ? "无明显突出维度" : factors.get(0).getName() + "（均分 " + factors.get(0).getAvgScore() + "）";
        String top2 = factors.size() < 2 ? "\u2014" : factors.get(1).getName() + "（均分 " + factors.get(1).getAvgScore() + "）";
        return ("本报告基于量表数据自动生成，仅供自我了解参考，不能替代专业医疗诊断。\n\n" +
                "## 一、总体筛查结果简报\n" +
                "本次总分为 %d，总症状指数为 %.1f，阳性项目数为 %d，整体提示为 %s 区间。量表结果仅反映近期状态，不能作为确诊依据。\n\n" +
                "## 二、各维度症状详细解读（按均分降序）\n" +
                "当前相对突出的维度：%s、%s。请优先关注这些维度在学习、作息和人际中的实际影响，并持续观察两周以上的变化趋势。\n\n" +
                "## 三、综合分析与生活建议\n" +
                "建议从规律睡眠、行为激活（小步可完成任务）、每日 10 分钟呼吸放松入手；若伴随明显躯体不适，可同步做基础体检排查。\n\n" +
                "## 四、专业求助指引\n" +
                "若连续两周以上心境低落、兴趣明显下降、焦虑与躯体不适持续加重，或出现自伤想法，请尽快联系心理咨询师或精神科医生。").formatted(
                report.getTotalScore(),
                report.getTotalAvg() == null ? 0 : report.getTotalAvg(),
                report.getPositiveCount() == null ? 0 : report.getPositiveCount(),
                report.getTotalLevel() == null ? "待评估" : report.getTotalLevel(),
                top1,
                top2
        ).trim();
    }
}
