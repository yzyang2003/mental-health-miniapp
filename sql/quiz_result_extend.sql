-- 测评结果表扩展：便于结果页展示比例与 AI 预填
ALTER TABLE `quiz_result`
  ADD COLUMN `max_score` int DEFAULT NULL COMMENT '满分' AFTER `score`,
  ADD COLUMN `score_percent` int DEFAULT NULL COMMENT '得分占比 0-100' AFTER `max_score`,
  ADD COLUMN `ai_chat_hint` varchar(500) DEFAULT NULL COMMENT '给小爱的一句话开场' AFTER `suggestions`;
