package com.example.demo.module.consult.skills;

import com.example.demo.module.consult.emotion.CrisisLevelAssessmentService.CrisisLevel;
import com.example.demo.module.consult.emotion.EmotionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 危机干预流程服务。
 * <p>
 * 提供完整的危机干预流程：
 * <ul>
 *   <li>危机检测</li>
 *   <li>安全响应</li>
 *   <li>资源提供</li>
 *   <li>跟进关怀</li>
 * </ul>
 */
@Slf4j
@Service
public class CrisisInterventionService {

    /** 心理援助热线 */
    private static final String PSYCHOLOGICAL_HOTLINE = "400-161-9995";

    /** 危机响应模板 */
    private static final String CRISIS_RESPONSE = "如果你正在经历危机，请立即拨打全国24小时心理援助热线："
            + PSYCHOLOGICAL_HOTLINE + "，或联系学校心理咨询中心。你的生命很重要，有人愿意帮助你。";

    /**
     * 生成危机干预响应
     *
     * @param crisisLevel   危机等级
     * @param emotionResult 情绪识别结果
     * @return 危机干预响应
     */
    public CrisisInterventionResponse intervene(CrisisLevel crisisLevel, EmotionResult emotionResult) {
        CrisisInterventionResponse response = new CrisisInterventionResponse();
        response.setCrisisLevel(crisisLevel);

        switch (crisisLevel) {
            case CRISIS:
                // 紧急：立即提供热线
                response.setMessage(CRISIS_RESPONSE);
                response.setHotline(PSYCHOLOGICAL_HOTLINE);
                response.setRequiresImmediateAction(true);
                log.warn("紧急危机干预：{}", emotionResult);
                break;

            case HIGH:
                // 高危：提供热线 + 安全计划
                response.setMessage("我听到你现在的感受很痛苦。请记住，你并不孤单。\n\n"
                        + "如果你正在经历危机，请立即拨打全国24小时心理援助热线："
                        + PSYCHOLOGICAL_HOTLINE + "\n"
                        + "或联系学校心理咨询中心。你的生命很重要，有人愿意帮助你。");
                response.setHotline(PSYCHOLOGICAL_HOTLINE);
                response.setRequiresImmediateAction(false);
                log.warn("高危危机干预：{}", emotionResult);
                break;

            case MODERATE:
                // 中危：提供支持 + 资源
                response.setMessage("我理解你现在的感受。如果你愿意，我们可以一起想办法。\n\n"
                        + "如果需要专业帮助，可以拨打心理援助热线：" + PSYCHOLOGICAL_HOTLINE);
                response.setRequiresImmediateAction(false);
                log.info("中危危机干预：{}", emotionResult);
                break;

            case LOW:
            default:
                // 低危：提供支持
                response.setMessage("我会一直在这里陪着你，愿意和我聊聊吗？");
                response.setRequiresImmediateAction(false);
                break;
        }

        return response;
    }

    /**
     * 危机干预响应内部类
     */
    public static class CrisisInterventionResponse {
        private CrisisLevel crisisLevel;
        private String message;
        private String hotline;
        private boolean requiresImmediateAction;

        // Getters and Setters
        public CrisisLevel getCrisisLevel() { return crisisLevel; }
        public void setCrisisLevel(CrisisLevel crisisLevel) { this.crisisLevel = crisisLevel; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getHotline() { return hotline; }
        public void setHotline(String hotline) { this.hotline = hotline; }
        public boolean isRequiresImmediateAction() { return requiresImmediateAction; }
        public void setRequiresImmediateAction(boolean requiresImmediateAction) { this.requiresImmediateAction = requiresImmediateAction; }

        @Override
        public String toString() {
            return "CrisisInterventionResponse{" +
                    "crisisLevel=" + crisisLevel +
                    ", message='" + message + '\'' +
                    ", hotline='" + hotline + '\'' +
                    ", requiresImmediateAction=" + requiresImmediateAction +
                    '}';
        }
    }
}
