package com.example.demo.module.consult.safety;

/**
 * 内容安全检查结果枚举。
 */
public enum SafetyResult {

    /** 安全，正常处理 */
    SAFE,

    /** 不安全，拒绝处理 */
    UNSAFE,

    /** 危机状态，需要紧急响应 */
    CRISIS
}
