-- 管理后台迁移：admin 用户表、notice 公告表、chat_history 情绪字段、user 角色字段
-- 执行方式：mysql -u root -p graduation_design < sql/admin_migration.sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 新建 admin 管理员表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '管理员用户名',
  `password_hash` varchar(255) NOT NULL COMMENT 'BCrypt密码哈希',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT 1 COMMENT '状态 0-停用 1-启用',
  `last_login` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- ----------------------------
-- 2. 新建 notice 公告表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '公告标题',
  `content` text DEFAULT NULL COMMENT '公告内容',
  `status` tinyint DEFAULT 1 COMMENT '状态 0-停用 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ----------------------------
-- 3. 插入默认管理员 (admin / admin123)
-- BCrypt hash generated with cost factor 10
-- ----------------------------
INSERT INTO `admin` (`username`, `password_hash`, `nickname`)
VALUES ('admin', '$2b$12$lhKn21gKNxpgYX5z72bwq.mO43E7bkVBwzY0BkhJEpCerNoUtcPp.', '超级管理员');

-- ----------------------------
-- 4. chat_history 添加 emotion 字段
-- ----------------------------
ALTER TABLE `chat_history`
  ADD COLUMN `emotion` varchar(20) DEFAULT NULL COMMENT '情绪标签（如 happy, sad, anxious）' AFTER `content`;

-- ----------------------------
-- 5. user 添加 role 字段
-- ----------------------------
ALTER TABLE `user`
  ADD COLUMN `role` varchar(20) DEFAULT 'user' COMMENT '用户角色（user/admin）' AFTER `gender`;

SET FOREIGN_KEY_CHECKS = 1;
