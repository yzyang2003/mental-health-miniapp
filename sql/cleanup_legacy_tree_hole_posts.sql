-- 早期树洞测试数据清理脚本
-- 使用前请先备份 graduation_design 库
-- 说明：
-- 1) 默认方案：按时间阈值删除（推荐）
-- 2) 备选方案：清空全部树洞帖子与回复

START TRANSACTION;

-- =========================
-- 方案一（推荐）：按时间阈值删除“早期测试贴”
-- 请按需要修改 @cutoff
-- 示例：删除 2026-04-15 00:00:00 之前的帖子及其回复
-- =========================
SET @cutoff = '2026-04-15 00:00:00';

-- 先删这些帖子下的所有回复
DELETE r
FROM `reply` r
JOIN `topic` t ON t.`id` = r.`topic_id`
WHERE t.`create_time` < @cutoff;

-- 再删帖子
DELETE FROM `topic`
WHERE `create_time` < @cutoff;

-- =========================
-- 方案二（备选）：如果你要“清空全部树洞数据”，取消下面注释
-- =========================
-- DELETE FROM `reply`;
-- DELETE FROM `topic`;
-- ALTER TABLE `reply` AUTO_INCREMENT = 1;
-- ALTER TABLE `topic` AUTO_INCREMENT = 1;

COMMIT;

-- 校验剩余数量
SELECT COUNT(*) AS topic_count FROM `topic`;
SELECT COUNT(*) AS reply_count FROM `reply`;
