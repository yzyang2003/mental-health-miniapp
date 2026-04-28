-- 将旧版 quiz_result 表补齐为与 Java 实体一致（解决 Unknown column 'max_score' 等插入失败）
-- 可重复执行：已存在的列会跳过。
-- 用法：在目标库执行本文件后重启后端，再提交测评。

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_quiz_result_ensure_columns$$

CREATE PROCEDURE sp_quiz_result_ensure_columns()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quiz_result' AND COLUMN_NAME = 'max_score'
  ) THEN
    ALTER TABLE `quiz_result`
      ADD COLUMN `max_score` int DEFAULT NULL COMMENT '满分' AFTER `score`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quiz_result' AND COLUMN_NAME = 'score_percent'
  ) THEN
    ALTER TABLE `quiz_result`
      ADD COLUMN `score_percent` int DEFAULT NULL COMMENT '得分占比 0-100' AFTER `max_score`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quiz_result' AND COLUMN_NAME = 'ai_chat_hint'
  ) THEN
    ALTER TABLE `quiz_result`
      ADD COLUMN `ai_chat_hint` varchar(500) DEFAULT NULL COMMENT '给小爱的一句话开场' AFTER `suggestions`;
  END IF;

  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'quiz_result' AND COLUMN_NAME = 'ai_guidance'
  ) THEN
    ALTER TABLE `quiz_result`
      ADD COLUMN `ai_guidance` text DEFAULT NULL COMMENT 'AI 生成的解读与建议（可空）' AFTER `ai_chat_hint`;
  END IF;
END$$

DELIMITER ;

CALL sp_quiz_result_ensure_columns();
DROP PROCEDURE IF EXISTS sp_quiz_result_ensure_columns;
