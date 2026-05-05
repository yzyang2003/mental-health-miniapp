package com.example.demo.module.consult.client;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI 兼容 Chat Completions 单次调用（不写 chat_history），供咨询聊天与测评 AI 报告共用 {@code ai.api.*}。
 */
@Component
@Slf4j
public class OpenAiCompletionClient {

    @Value("${ai.api.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${ai.api.read-timeout:30000}")
    private int readTimeout;

    @Value("${ai.api.max-retry:1}")
    private int maxRetry;

    private RestTemplate restTemplate;

    @Value("${ai.api.url:}")
    private String aiApiUrl;

    @Value("${ai.api.key:}")
    private String aiApiKey;

    @Value("${ai.api.model:}")
    private String aiModel;

    /**
     * 速度优先：限制输出 token，避免长文本拖慢响应。
     */
    @Value("${ai.api.max-tokens:700}")
    private Integer aiMaxTokens;

    /**
     * 速度优先且保持稳定风格，降低随机性。
     */
    @Value("${ai.api.temperature:0.4}")
    private Double aiTemperature;

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        this.restTemplate = new RestTemplate(factory);
        log.info("OpenAiCompletionClient initialized: connectTimeout={}ms, readTimeout={}ms, maxRetry={}",
                connectTimeout, readTimeout, maxRetry);
    }

    /**
     * 调用大模型；未配置 URL/Key、请求失败或解析不到正文时返回 {@code null}（由调用方决定是否降级）。
     */
    public String complete(List<Map<String, String>> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        if (!StringUtils.hasText(aiApiUrl) || !StringUtils.hasText(aiApiKey)) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiApiKey);

        Map<String, Object> body = new HashMap<>();
        if (StringUtils.hasText(aiModel)) {
            body.put("model", aiModel);
        }
        body.put("messages", messages);
        if (aiMaxTokens != null && aiMaxTokens > 0) {
            body.put("max_tokens", aiMaxTokens);
        }
        if (aiTemperature != null) {
            body.put("temperature", aiTemperature);
        }

        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            try {
                Map<?, ?> response = restTemplate.postForObject(
                        aiApiUrl,
                        new HttpEntity<>(body, headers),
                        Map.class
                );
                String content = extractAssistantContent(response);
                if (StringUtils.hasText(content)) {
                    return content;
                }
                if (attempt < maxRetry) {
                    log.warn("OpenAI-compatible completion empty content, retrying attempt={}", attempt + 1);
                }
            } catch (Exception ex) {
                if (attempt >= maxRetry) {
                    log.warn("OpenAI-compatible completion request failed after {} retries", maxRetry, ex);
                    return null;
                }
                log.warn("OpenAI-compatible completion request failed, retrying attempt={}/{}: {}",
                        attempt + 1, maxRetry, ex.getMessage());
                try {
                    Thread.sleep(1000L * (attempt + 1)); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Retry sleep interrupted, aborting retries");
                    return null;
                }
            }
        }
        return null;
    }

    private static String extractAssistantContent(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            return null;
        }
        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) {
            return null;
        }
        Object contentObj = messageMap.get("content");
        String content = normalizeContent(contentObj);
        if (StringUtils.hasText(content)) {
            return content;
        }
        // MiMo 等推理模型：思考过程在 reasoning_content，content 可能为空
        Object reasoningObj = messageMap.get("reasoning_content");
        String reasoning = normalizeContent(reasoningObj);
        if (StringUtils.hasText(reasoning)) {
            log.info("Using reasoning_content as assistant reply (content was empty)");
            return reasoning;
        }
        return content;
    }

    /**
     * 兼容不同 OpenAI 兼容网关的返回结构：
     * 1) content 为字符串
     * 2) content 为数组（text/plain 或 {"type":"text","text":"..."}）
     */
    private static String normalizeContent(Object contentObj) {
        if (contentObj instanceof String content) {
            return StringUtils.hasText(content) ? content.trim() : null;
        }
        if (contentObj instanceof List<?> list && !list.isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof String s && StringUtils.hasText(s)) {
                    parts.add(s.trim());
                    continue;
                }
                if (item instanceof Map<?, ?> itemMap) {
                    Object textObj = itemMap.get("text");
                    if (textObj instanceof String text && StringUtils.hasText(text)) {
                        parts.add(text.trim());
                    }
                }
            }
            if (!parts.isEmpty()) {
                return String.join("\n", parts);
            }
        }
        return null;
    }
}
