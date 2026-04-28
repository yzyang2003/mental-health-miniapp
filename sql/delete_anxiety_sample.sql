-- 删除「焦虑自评量表（示例）」及其关联题目/结果（保留 PHQ/GAD 教学演示量表）
-- 适用场景：已导入示例种子，但现在只想保留教学演示量表。
-- 执行前请确认当前库为 graduation_design。

START TRANSACTION;

CREATE TEMPORARY TABLE tmp_del_questionnaire AS
SELECT id
FROM questionnaire
WHERE title = '焦虑自评量表（示例）'
   OR type = '焦虑';

DELETE q
FROM question q
JOIN tmp_del_questionnaire d ON q.questionnaire_id = d.id;

DELETE r
FROM quiz_result r
JOIN tmp_del_questionnaire d ON r.questionnaire_id = d.id;

DELETE qq
FROM questionnaire qq
JOIN tmp_del_questionnaire d ON qq.id = d.id;

DROP TEMPORARY TABLE IF EXISTS tmp_del_questionnaire;

COMMIT;
