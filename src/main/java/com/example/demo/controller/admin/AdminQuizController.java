package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.module.consult.entity.Question;
import com.example.demo.module.consult.entity.Questionnaire;
import com.example.demo.module.consult.mapper.QuestionMapper;
import com.example.demo.module.consult.mapper.QuestionnaireMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 问卷管理控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/quiz")
@RequiredArgsConstructor
public class AdminQuizController {

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;

    /**
     * 问卷列表（含题目数）。
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<Questionnaire> questionnaires = questionnaireMapper.selectList(
                new LambdaQueryWrapper<Questionnaire>()
                        .orderByDesc(Questionnaire::getCreateTime)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (Questionnaire q : questionnaires) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", q.getId());
            item.put("title", q.getTitle());
            item.put("description", q.getDescription());
            item.put("cover", q.getCover());
            item.put("type", q.getType());
            item.put("status", q.getStatus());
            item.put("createTime", q.getCreateTime());

            // 查询题目数
            Long questionCount = questionMapper.selectCount(
                    new LambdaQueryWrapper<Question>()
                            .eq(Question::getQuestionnaireId, q.getId())
            );
            item.put("questionCount", questionCount);
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 问卷详情。
     */
    @GetMapping("/{id}")
    public Result<Questionnaire> detail(@PathVariable Long id) {
        Questionnaire questionnaire = questionnaireMapper.selectById(id);
        if (questionnaire == null) {
            return Result.error(404, "问卷不存在");
        }
        return Result.success(questionnaire);
    }

    /**
     * 新增问卷。
     */
    @PostMapping
    public Result<Map<String, Long>> create(@RequestBody Questionnaire questionnaire) {
        questionnaire.setCreateTime(LocalDateTime.now());
        questionnaire.setUpdateTime(LocalDateTime.now());
        questionnaireMapper.insert(questionnaire);
        return Result.success(Map.of("id", questionnaire.getId()));
    }

    /**
     * 编辑问卷。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Questionnaire questionnaire) {
        questionnaire.setId(id);
        questionnaire.setUpdateTime(LocalDateTime.now());
        questionnaireMapper.updateById(questionnaire);
        return Result.success(null);
    }

    /**
     * 启用/停用问卷。
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.error(400, "状态不能为空");
        }
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(id);
        questionnaire.setStatus(status);
        questionnaire.setUpdateTime(LocalDateTime.now());
        questionnaireMapper.updateById(questionnaire);
        return Result.success(null);
    }

    /**
     * 删除问卷（同时删除关联题目）。
     */
    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> delete(@PathVariable Long id) {
        // 先删除关联题目
        questionMapper.delete(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionnaireId, id)
        );
        // 再删除问卷
        questionnaireMapper.deleteById(id);
        return Result.success(null);
    }

    /**
     * 获取某问卷的所有题目。
     */
    @GetMapping("/{id}/questions")
    public Result<List<Question>> getQuestions(@PathVariable Long id) {
        List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionnaireId, id)
                        .orderByAsc(Question::getSortOrder)
        );
        return Result.success(questions);
    }

    /**
     * 批量保存题目（全量替换）。
     */
    @PostMapping("/{id}/questions")
    @Transactional
    public Result<Void> saveQuestions(@PathVariable Long id, @RequestBody List<Question> questions) {
        // 删除原有题目
        questionMapper.delete(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getQuestionnaireId, id)
        );

        // 插入新题目
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            question.setId(null); // 确保新增
            question.setQuestionnaireId(id);
            if (question.getSortOrder() == null) {
                question.setSortOrder(i + 1);
            }
            if (question.getStatus() == null) {
                question.setStatus(1);
            }
            questionMapper.insert(question);
        }

        return Result.success(null);
    }
}
