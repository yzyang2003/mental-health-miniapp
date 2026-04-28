-- 测评结果表：AI 个性化指导文案（与咨询师共用 ai.api.*，单次生成不落 chat_history）
ALTER TABLE `quiz_result`
  ADD COLUMN `ai_guidance` text DEFAULT NULL COMMENT 'AI 生成的解读与建议（可空，失败时保留规则文案）' AFTER `ai_chat_hint`;
