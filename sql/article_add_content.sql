-- 为心理文章增加正文字段（在已有库上执行一次即可；若已存在该列会报错，可忽略）
ALTER TABLE `article`
  ADD COLUMN `content` mediumtext COMMENT '正文（纯文本，可含换行）' AFTER `summary`;
