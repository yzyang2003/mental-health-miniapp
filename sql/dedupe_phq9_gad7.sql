-- 修复 PHQ-9 / GAD-7 问卷与题目重复（翻倍）问题
-- 作用：
-- 1) 若 questionnaire 中同 type 存在多条，仅保留最小 id 的一条
-- 2) 自动把 quiz_result.questionnaire_id 迁移到保留的 id，避免历史结果丢失
-- 3) 删除被淘汰问卷下的题目
-- 4) 对保留问卷按 sort_order 去重（同题序仅保留最小 id）

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_quiz_keep;
CREATE TEMPORARY TABLE tmp_quiz_keep (
  type VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY,
  keep_id BIGINT NOT NULL
);

INSERT INTO tmp_quiz_keep (type, keep_id)
SELECT q.type, MIN(q.id) AS keep_id
FROM questionnaire q
WHERE q.type IN ('PHQ9_DEMO', 'GAD7_DEMO')
GROUP BY q.type;

-- 先迁移历史测评结果，避免删问卷后产生悬挂外键/脏数据
UPDATE quiz_result r
JOIN questionnaire q ON q.id = r.questionnaire_id
JOIN tmp_quiz_keep k ON k.type = q.type COLLATE utf8mb4_unicode_ci
SET r.questionnaire_id = k.keep_id
WHERE q.id <> k.keep_id;

-- 删除重复问卷下的题目
DELETE ques
FROM question ques
JOIN questionnaire q ON q.id = ques.questionnaire_id
JOIN tmp_quiz_keep k ON k.type = q.type COLLATE utf8mb4_unicode_ci
WHERE q.id <> k.keep_id;

-- 删除重复问卷（同 type）
DELETE q
FROM questionnaire q
JOIN tmp_quiz_keep k ON k.type = q.type COLLATE utf8mb4_unicode_ci
WHERE q.id <> k.keep_id;

-- 保留问卷内再按 sort_order 去重（防止同一问卷被重复灌题）
DELETE q1
FROM question q1
JOIN question q2
  ON q1.questionnaire_id = q2.questionnaire_id
 AND q1.sort_order = q2.sort_order
 AND q1.id > q2.id
JOIN questionnaire qq ON qq.id = q1.questionnaire_id
WHERE qq.type IN ('PHQ9_DEMO', 'GAD7_DEMO');

DROP TEMPORARY TABLE IF EXISTS tmp_quiz_keep;

COMMIT;
