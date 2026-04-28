package com.example.demo.module.consult.dto;

import lombok.Data;

/**
 * SCL-90 分因子结果。
 */
@Data
public class Scl90FactorVO {

    private String name;

    private Integer totalScore;

    private Double avgScore;

    private String level;

    private String reference;

    private String summary;
}
