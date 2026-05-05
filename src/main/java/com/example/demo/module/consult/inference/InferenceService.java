package com.example.demo.module.consult.inference;

import java.util.List;
import java.util.Map;

/**
 * AI 推理服务抽象层，屏蔽底层模型调用差异。
 */
public interface InferenceService {

    /**
     * 执行 AI 推理（单次 completion）。
     *
     * @param messages 消息列表，每条包含 role 和 content
     * @return AI 回复内容；调用失败或未配置时返回 {@code null}
     */
    String complete(List<Map<String, String>> messages);

    /**
     * 检查服务是否可用（配置完整且可连通）。
     *
     * @return true 如果服务可用
     */
    boolean isAvailable();
}
