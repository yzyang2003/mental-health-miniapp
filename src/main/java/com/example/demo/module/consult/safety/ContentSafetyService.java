package com.example.demo.module.consult.safety;

/**
 * 内容安全检测服务接口。
 */
public interface ContentSafetyService {

    /**
     * 检查内容安全性。
     *
     * @param content 用户输入内容
     * @return 安全检查结果
     */
    SafetyResult check(String content);
}
