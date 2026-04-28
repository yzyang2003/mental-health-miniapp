ALTER TABLE `reply`
ADD COLUMN `replied_reply_id` bigint DEFAULT NULL COMMENT '被回复的回复ID（用于级联删除）' AFTER `replied_user_openid`,
ADD KEY `idx_replied_reply_id` (`replied_reply_id`);
