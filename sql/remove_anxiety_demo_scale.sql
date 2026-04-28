-- 删除「焦虑自评量表（示例）」及其关联题目与测评记录
-- 使用场景：不再展示该示例问卷，仅保留 PHQ/GAD 教学演示量表
-- 请在目标库（如 graduation_design）执行。

START TRANSACTION;

CREATE TEMPORARY TABLE tmp_anxiety_demo AS
SELECT id
FROM questionnaire
WHERE title = '焦虑自评量表（示例）'
   OR (type = '焦虑' AND title LIKE '%示例%');

DELETE qn
FROM question qn
JOIN tmp_anxiety_demo t ON qn.questionnaire_id = t.id;

DELETE qr
FROM quiz_result qr
JOIN tmp_anxiety_demo t ON qr.questionnaire_id = t.id;

DELETE q
FROM questionnaire q
JOIN tmp_anxiety_demo t ON q.id = t.id;

DROP TEMPORARY TABLE IF EXISTS tmp_anxiety_demo;

COMMIT;
