-- SCL-90因子配置表
CREATE TABLE IF NOT EXISTS `scl90_factor_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `factor_name` VARCHAR(50) NOT NULL COMMENT '因子名称',
  `item_numbers` VARCHAR(200) NOT NULL COMMENT '题目编号（逗号分隔）',
  `reference_value` VARCHAR(50) DEFAULT NULL COMMENT '常模参考值',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_factor_name` (`factor_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SCL-90因子配置';

-- 插入默认数据
INSERT INTO `scl90_factor_config` (`factor_name`, `item_numbers`, `reference_value`, `sort_order`) VALUES
('躯体化', '1,4,12,27,40,42,48,49,52,53,56,58', '1.37±0.48', 1),
('强迫症状', '3,9,10,28,38,45,46,51,55,65', '1.62±0.58', 2),
('人际敏感', '6,21,34,36,37,41,61,69,73', '1.65±0.61', 3),
('抑郁', '5,14,15,20,22,26,29,30,31,32,54,71,79', '1.50±0.59', 4),
('焦虑', '2,17,23,33,39,57,72,78,80,86', '1.39±0.43', 5),
('敌对', '11,24,63,67,74,81', '1.46±0.55', 6),
('恐怖', '13,25,47,50,70,75,82', '1.23±0.41', 7),
('偏执', '8,18,43,68,76,83', '1.43±0.57', 8),
('精神病性', '7,16,35,62,77,84,85,87,88,90', '1.29±0.42', 9),
('其他', '19,44,59,60,64,66,89', '-', 10);
