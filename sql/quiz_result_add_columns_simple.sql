-- 旧版 quiz_result 表补齐字段（与 Java 实体一致）
-- 适用于 Navicat：选中库 graduation_design 后，可整段执行；若某列已存在会报错，跳过该行即可。
-- 执行后重启 Spring Boot。

ALTER TABLE `quiz_result`
  ADD COLUMN `max_score` int DEFAULT NULL COMMENT '满分' AFTER `score`;

ALTER TABLE `quiz_result`
  ADD COLUMN `score_percent` int DEFAULT NULL COMMENT '得分占比 0-100' AFTER `max_score`;

ALTER TABLE `quiz_result`
  ADD COLUMN `ai_chat_hint` varchar(500) DEFAULT NULL COMMENT '给小爱的一句话开场' AFTER `suggestions`;

ALTER TABLE `quiz_result`
  ADD COLUMN `ai_guidance` text DEFAULT NULL COMMENT 'AI 生成的解读与建议（可空）' AFTER `ai_chat_hint`;
