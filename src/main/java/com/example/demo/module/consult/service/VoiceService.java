package com.example.demo.module.consult.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 语音服务 - 封装MiMo ASR和TTS API调用
 */
@Slf4j
@Service
public class VoiceService {

    @Value("${mimo.api.key:}")
    private String mimoApiKey;

    @Value("${mimo.api.base-url:https://api.xiaomimimo.com}")
    private String mimoBaseUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 语音转文字（ASR）
     * 使用MiMo-V2.5多模态模型处理音频
     */
    public String speechToText(MultipartFile audioFile) throws IOException, InterruptedException {
        if (mimoApiKey == null || mimoApiKey.isEmpty()) {
            throw new RuntimeException("MiMo API Key未配置");
        }

        // 将音频文件转为Base64
        byte[] audioBytes = audioFile.getBytes();
        String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

        // 构建请求体 - 使用多模态格式
        Map<String, Object> requestBody = Map.of(
            "model", "mimo-v2.5",
            "messages", new Object[]{
                Map.of("role", "user", "content", new Object[]{
                    Map.of("type", "audio_url", "audio_url", Map.of("url", "data:audio/mp3;base64," + audioBase64)),
                    Map.of("type", "text", "text", "请将这段音频转录为文字，只输出转录结果，不要添加任何其他内容")
                })
            },
            "max_tokens", 1024
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(mimoBaseUrl + "/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + mimoApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("ASR请求失败: {}", response.body());
            throw new RuntimeException("语音识别失败: " + response.statusCode());
        }

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        if (responseMap.containsKey("choices")) {
            var choices = (java.util.List<?>) responseMap.get("choices");
            if (!choices.isEmpty()) {
                var choice = (Map<String, Object>) choices.get(0);
                var message = (Map<String, Object>) choice.get("message");
                if (message != null && message.containsKey("content")) {
                    return (String) message.get("content");
                }
            }
        }

        throw new RuntimeException("语音识别返回格式异常");
    }

    /**
     * 文字转语音（TTS）
     * 使用MiMo-V2.5-TTS模型
     */
    public String textToSpeech(String text) throws IOException, InterruptedException {
        if (mimoApiKey == null || mimoApiKey.isEmpty()) {
            throw new RuntimeException("MiMo API Key未配置");
        }

        // 构建请求体 - 使用OpenAI兼容格式
        Map<String, Object> requestBody = Map.of(
            "model", "mimo-v2-tts",
            "messages", new Object[]{
                Map.of("role", "user", "content", "请用温柔的语气说出以下内容"),
                Map.of("role", "assistant", "content", text)
            },
            "audio", Map.of(
                "format", "wav",
                "voice", "default_zh"
            )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(mimoBaseUrl + "/chat/completions"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + mimoApiKey)
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("TTS请求失败: {}", response.body());
            throw new RuntimeException("语音合成失败: " + response.statusCode());
        }

        // 解析响应
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        if (responseMap.containsKey("choices")) {
            var choices = (java.util.List<?>) responseMap.get("choices");
            if (!choices.isEmpty()) {
                var choice = (Map<String, Object>) choices.get(0);
                var message = (Map<String, Object>) choice.get("message");
                if (message != null && message.containsKey("audio")) {
                    var audio = (Map<String, Object>) message.get("audio");
                    if (audio != null && audio.containsKey("data")) {
                        String audioBase64 = (String) audio.get("data");
                        // 返回Base64编码的音频数据
                        return "data:audio/wav;base64," + audioBase64;
                    }
                }
            }
        }

        throw new RuntimeException("语音合成返回格式异常");
    }
}
