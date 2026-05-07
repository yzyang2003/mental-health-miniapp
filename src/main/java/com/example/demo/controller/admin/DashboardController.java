package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.dto.admin.DashboardStatsVO;
import com.example.demo.entity.User;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.module.consult.entity.ChatHistory;
import com.example.demo.module.consult.entity.Questionnaire;
import com.example.demo.module.consult.entity.QuizResult;
import com.example.demo.module.consult.mapper.ChatHistoryMapper;
import com.example.demo.module.consult.mapper.QuestionnaireMapper;
import com.example.demo.module.consult.mapper.QuizResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard 统计控制器。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserMapper userMapper;
    private final TopicMapper topicMapper;
    private final QuizResultMapper quizResultMapper;
    private final ChatHistoryMapper chatHistoryMapper;
    private final QuestionnaireMapper questionnaireMapper;

    /**
     * 获取 Dashboard 统计数据。
     */
    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        stats.setTotalUsers(userMapper.selectCount(null));

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        stats.setTodayNew(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)
        ));

        stats.setTotalTopics(topicMapper.selectCount(null));
        stats.setTotalQuizCount(quizResultMapper.selectCount(null));
        stats.setTotalChatMessages(chatHistoryMapper.selectCount(null));

        // 近 7 天测评趋势
        List<DashboardStatsVO.TrendItem> trendData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Long count = quizResultMapper.selectCount(
                    new LambdaQueryWrapper<QuizResult>()
                            .ge(QuizResult::getCreateTime, dayStart)
                            .le(QuizResult::getCreateTime, dayEnd)
            );
            DashboardStatsVO.TrendItem item = new DashboardStatsVO.TrendItem();
            item.setDate(date.toString());
            item.setCount(count);
            trendData.add(item);
        }
        stats.setTrendData(trendData);

        // 情绪分布
        Map<String, Long> emotionDistribution = new HashMap<>();
        List<ChatHistory> chatWithEmotion = chatHistoryMapper.selectList(
                new LambdaQueryWrapper<ChatHistory>()
                        .isNotNull(ChatHistory::getEmotion)
                        .ne(ChatHistory::getEmotion, "")
        );
        for (ChatHistory chat : chatWithEmotion) {
            emotionDistribution.merge(chat.getEmotion(), 1L, Long::sum);
        }
        stats.setEmotionDistribution(emotionDistribution);

        // 近 7 天用户注册趋势
        List<DashboardStatsVO.TrendItem> userTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .ge(User::getCreateTime, dayStart)
                            .le(User::getCreateTime, dayEnd)
            );
            DashboardStatsVO.TrendItem item = new DashboardStatsVO.TrendItem();
            item.setDate(date.toString());
            item.setCount(count);
            userTrend.add(item);
        }
        stats.setUserTrend(userTrend);

        // 近 7 天日活用户
        List<DashboardStatsVO.TrendItem> dailyActive = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            List<ChatHistory> chats = chatHistoryMapper.selectList(
                    new LambdaQueryWrapper<ChatHistory>()
                            .ge(ChatHistory::getCreateTime, dayStart)
                            .le(ChatHistory::getCreateTime, dayEnd)
            );
            long uniqueUsers = chats.stream()
                    .map(ChatHistory::getOpenid)
                    .distinct()
                    .count();
            DashboardStatsVO.TrendItem item = new DashboardStatsVO.TrendItem();
            item.setDate(date.toString());
            item.setCount(uniqueUsers);
            dailyActive.add(item);
        }
        stats.setDailyActive(dailyActive);

        // 测评量表使用率
        Map<String, Long> quizByType = new LinkedHashMap<>();
        List<QuizResult> allResults = quizResultMapper.selectList(null);
        Map<Long, Long> grouped = allResults.stream()
                .collect(Collectors.groupingBy(QuizResult::getQuestionnaireId, Collectors.counting()));
        List<Questionnaire> questionnaires = questionnaireMapper.selectList(null);
        Map<Long, String> idToTitle = questionnaires.stream()
                .collect(Collectors.toMap(Questionnaire::getId, Questionnaire::getTitle));
        grouped.forEach((qId, cnt) -> {
            String title = idToTitle.getOrDefault(qId, "问卷#" + qId);
            quizByType.put(title, cnt);
        });
        stats.setQuizByType(quizByType);

        return Result.success(stats);
    }
}
