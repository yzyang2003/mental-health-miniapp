package com.example.demo.module.consult.inference;

import com.example.demo.module.consult.client.OpenAiCompletionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 基于 OpenAI 兼容接口的推理服务实现。
 * <p>
 * 委托 {@link OpenAiCompletionClient} 完成实际 HTTP 调用，
 * 本层仅做异常兜底与可用性判断。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiInferenceService implements InferenceService {

    private final OpenAiCompletionClient openAiCompletionClient;

    @Override
    public String complete(List<Map<String, String>> messages) {
        try {
            return openAiCompletionClient.complete(messages);
        } catch (Exception e) {
            log.error("AI inference failed", e);
            return null;
        }
    }

    @Override
    public boolean isAvailable() {
        return openAiCompletionClient != null;
    }
}
