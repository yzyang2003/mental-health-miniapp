-- 将已存在的演示量表名称统一为答辩展示名称
-- 适用场景：数据库中已导入过旧种子，标题仍为“教学演示版”

UPDATE `questionnaire`
SET
  `title` = '症状自评量表（SCL-90）',
  `description` = REPLACE(`description`, '教学演示', '答辩演示')
WHERE `type` = 'SCL90_DEMO';

UPDATE `questionnaire`
SET
  `title` = '抑郁症筛查量表（PHQ-9）',
  `description` = REPLACE(`description`, '教学演示', '答辩演示')
WHERE `type` = 'PHQ9_DEMO';

UPDATE `questionnaire`
SET
  `title` = '焦虑症筛查量表（GAD-7）',
  `description` = REPLACE(`description`, '教学演示', '答辩演示')
WHERE `type` = 'GAD7_DEMO';
