package com.example.demo.module.consult.dto;

import lombok.Data;

import java.util.List;

/**
 * SCL-90 结果报告。
 */
@Data
public class Scl90ReportVO {

    private Integer totalScore;

    private Double totalAvg;

    private Integer positiveCount;

    private Integer negativeCount;

    private Double positiveAvg;

    private String totalLevel;

    private String overview;

    private List<Scl90FactorVO> factors;
}
