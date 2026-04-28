-- 基础测评种子（仅保留答辩演示量表）
-- 说明：已移除「焦虑自评量表（示例）」；如需回滚请自行补充业务问卷。
-- 注意：若已执行过 `professional_demo_scales.sql`，请勿再执行本文件，避免 PHQ/GAD 重复插入。

SET @opts_phq = JSON_ARRAY(
  JSON_OBJECT('text', '完全不会', 'score', 0),
  JSON_OBJECT('text', '好几天', 'score', 1),
  JSON_OBJECT('text', '一半以上的天数', 'score', 2),
  JSON_OBJECT('text', '几乎每天', 'score', 3)
);

INSERT INTO `questionnaire` (`title`, `description`, `cover`, `type`, `scoring_rule`, `status`)
SELECT
    '抑郁症筛查量表（PHQ-9）',
    '9项抑郁症快速筛查，题目计分适配演示对接。划界规则为答辩演示设定，非认证 PHQ-9 标准。分数仅作流程示意，不作为临床诊断依据。',
    NULL,
    'PHQ9_DEMO',
    NULL,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `questionnaire` WHERE `type` = 'PHQ9_DEMO'
);
SET @phq_id = (
  SELECT `id` FROM `questionnaire` WHERE `type` = 'PHQ9_DEMO' ORDER BY `id` ASC LIMIT 1
);

INSERT INTO `question` (`questionnaire_id`, `content`, `options`, `sort_order`, `status`)
SELECT @phq_id, t.content, @opts_phq, t.sort_order, 1
FROM (
  SELECT 1 AS sort_order, '做事时提不起劲或没有乐趣' AS content
  UNION ALL SELECT 2, '感到心情低落、沮丧或绝望'
  UNION ALL SELECT 3, '入睡困难、睡不安稳或睡得太多'
  UNION ALL SELECT 4, '感到疲倦或没有力气'
  UNION ALL SELECT 5, '胃口不好或吃太多'
  UNION ALL SELECT 6, '觉得自己很糟，或让自己或家人失望'
  UNION ALL SELECT 7, '对事物难以集中注意力（例如阅读或看电视时）'
  UNION ALL SELECT 8, '动作或说话缓慢到别人察觉，或相反烦躁不安、动来动去'
  UNION ALL SELECT 9, '有不如死掉或用某种方式伤害自己的念头'
) t
LEFT JOIN `question` q
  ON q.questionnaire_id = @phq_id
 AND q.sort_order = t.sort_order
WHERE q.id IS NULL;

SET @opts_gad = JSON_ARRAY(
  JSON_OBJECT('text', '完全不会', 'score', 0),
  JSON_OBJECT('text', '好几天', 'score', 1),
  JSON_OBJECT('text', '一半以上的天数', 'score', 2),
  JSON_OBJECT('text', '几乎每天', 'score', 3)
);

INSERT INTO `questionnaire` (`title`, `description`, `cover`, `type`, `scoring_rule`, `status`)
SELECT
    '焦虑症筛查量表（GAD-7）',
    '7项焦虑症状自评，题目简化为答辩演示呈现。划界阈值沿用演示规则，非临床认证 GAD-7。结果仅供功能展示，不可替代专业诊疗判断。',
    NULL,
    'GAD7_DEMO',
    NULL,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `questionnaire` WHERE `type` = 'GAD7_DEMO'
);
SET @gad_id = (
  SELECT `id` FROM `questionnaire` WHERE `type` = 'GAD7_DEMO' ORDER BY `id` ASC LIMIT 1
);

INSERT INTO `question` (`questionnaire_id`, `content`, `options`, `sort_order`, `status`)
SELECT @gad_id, t.content, @opts_gad, t.sort_order, 1
FROM (
  SELECT 1 AS sort_order, '感到紧张、焦虑或急切' AS content
  UNION ALL SELECT 2, '不能够停止或无法掌控担忧'
  UNION ALL SELECT 3, '对各种各样的事情担忧过多'
  UNION ALL SELECT 4, '很难放松下来'
  UNION ALL SELECT 5, '由于不安而无法静坐'
  UNION ALL SELECT 6, '变得容易烦恼或急躁'
  UNION ALL SELECT 7, '感到似乎将有可怕的事情发生而害怕'
) t
LEFT JOIN `question` q
  ON q.questionnaire_id = @gad_id
 AND q.sort_order = t.sort_order
WHERE q.id IS NULL;

SET @opts_zung = JSON_ARRAY(
  JSON_OBJECT('text', '没有或很少时间', 'score', 1),
  JSON_OBJECT('text', '小部分时间', 'score', 2),
  JSON_OBJECT('text', '相当多时间', 'score', 3),
  JSON_OBJECT('text', '绝大部分或全部时间', 'score', 4)
);

INSERT INTO `questionnaire` (`title`, `description`, `cover`, `type`, `scoring_rule`, `status`)
SELECT
    '抑郁自评量表（SDS）',
    '20项抑郁症状自评量表，4级频度作答。用于答辩演示与自助观察，不作为临床诊断依据。',
    NULL,
    'SDS_DEMO',
    NULL,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `questionnaire` WHERE `type` = 'SDS_DEMO'
);
SET @sds_id = (
  SELECT `id` FROM `questionnaire` WHERE `type` = 'SDS_DEMO' ORDER BY `id` ASC LIMIT 1
);

INSERT INTO `question` (`questionnaire_id`, `content`, `options`, `sort_order`, `status`)
SELECT @sds_id, t.content, @opts_zung, t.sort_order, 1
FROM (
  SELECT 1 AS sort_order, '我感到情绪沮丧、忧郁' AS content
  UNION ALL SELECT 2, '我感到早晨心情最好'
  UNION ALL SELECT 3, '我要哭或想哭'
  UNION ALL SELECT 4, '我夜间睡眠不好'
  UNION ALL SELECT 5, '我吃饭像平时一样多'
  UNION ALL SELECT 6, '我的性功能正常'
  UNION ALL SELECT 7, '我感到体重减轻'
  UNION ALL SELECT 8, '我为便秘烦恼'
  UNION ALL SELECT 9, '我的心跳比平时快'
  UNION ALL SELECT 10, '我无故感到疲劳'
  UNION ALL SELECT 11, '我的头脑像平时一样清楚'
  UNION ALL SELECT 12, '我做事像平时一样不感到困难'
  UNION ALL SELECT 13, '我感到不安、难以平静'
  UNION ALL SELECT 14, '我对未来抱有希望'
  UNION ALL SELECT 15, '我比平时更容易激怒'
  UNION ALL SELECT 16, '我觉得做决定容易'
  UNION ALL SELECT 17, '我感到自己是有用的、不可缺少的人'
  UNION ALL SELECT 18, '我的生活很有意义'
  UNION ALL SELECT 19, '假若我死了别人会过得更好'
  UNION ALL SELECT 20, '我仍旧喜爱自己平时喜爱的事物'
) t
LEFT JOIN `question` q
  ON q.questionnaire_id = @sds_id
 AND q.sort_order = t.sort_order
WHERE q.id IS NULL;

INSERT INTO `questionnaire` (`title`, `description`, `cover`, `type`, `scoring_rule`, `status`)
SELECT
    '焦虑自评量表（SAS）',
    '20项焦虑症状自评量表，4级频度作答。用于答辩演示与自助观察，不作为临床诊断依据。',
    NULL,
    'SAS_DEMO',
    NULL,
    1
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `questionnaire` WHERE `type` = 'SAS_DEMO'
);
SET @sas_id = (
  SELECT `id` FROM `questionnaire` WHERE `type` = 'SAS_DEMO' ORDER BY `id` ASC LIMIT 1
);

INSERT INTO `question` (`questionnaire_id`, `content`, `options`, `sort_order`, `status`)
SELECT @sas_id, t.content, @opts_zung, t.sort_order, 1
FROM (
  SELECT 1 AS sort_order, '我觉得比平常容易紧张和着急' AS content
  UNION ALL SELECT 2, '我无缘无故感到害怕'
  UNION ALL SELECT 3, '我容易心里烦乱或感到惊恐'
  UNION ALL SELECT 4, '我觉得我可能要发疯'
  UNION ALL SELECT 5, '我觉得一切都很好，也不会发生什么不幸'
  UNION ALL SELECT 6, '我手脚发抖打颤'
  UNION ALL SELECT 7, '我因为头痛、颈痛和背痛而苦恼'
  UNION ALL SELECT 8, '我感觉容易衰弱和疲乏'
  UNION ALL SELECT 9, '我觉得心平气和，并且容易安静坐着'
  UNION ALL SELECT 10, '我觉得心跳得很快'
  UNION ALL SELECT 11, '我因为一阵阵头晕而苦恼'
  UNION ALL SELECT 12, '我有晕倒发作或觉得要晕倒似的'
  UNION ALL SELECT 13, '我吸气呼气都感到很容易'
  UNION ALL SELECT 14, '我的手脚麻木和刺痛'
  UNION ALL SELECT 15, '我因为胃痛和消化不良而苦恼'
  UNION ALL SELECT 16, '我常常要小便'
  UNION ALL SELECT 17, '我的手常常是干燥温暖的'
  UNION ALL SELECT 18, '我脸红发热'
  UNION ALL SELECT 19, '我容易入睡并且一夜睡得很好'
  UNION ALL SELECT 20, '我做恶梦'
) t
LEFT JOIN `question` q
  ON q.questionnaire_id = @sas_id
 AND q.sort_order = t.sort_order
WHERE q.id IS NULL;


