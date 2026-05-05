package com.example.demo.module.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SCL-90因子配置实体。
 */
@Data
@TableName("scl90_factor_config")
public class Scl90FactorConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String factorName;

    private String itemNumbers;

    private String referenceValue;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
