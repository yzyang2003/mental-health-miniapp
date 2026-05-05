package com.example.demo.module.consult.service;

import org.springframework.stereotype.Service;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long ONE_MINUTE_MS = 60_000L;

    private final ConcurrentHashMap<String, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

    /**
     * 检查是否超过速率限制
     * @param key 限流键（通常是openid）
     * @return true如果允许请求，false如果超过限制
     */
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = requestTimestamps.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // 清理超过1分钟的记录
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > ONE_MINUTE_MS) {
            timestamps.pollFirst();
        }

        // 检查是否超过限制
        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            return false;
        }

        // 记录本次请求
        timestamps.addLast(now);
        return true;
    }

    /**
     * 获取剩余等待时间（秒）
     */
    public int getRemainingWaitSeconds(String key) {
        Deque<Long> timestamps = requestTimestamps.get(key);
        if (timestamps == null || timestamps.isEmpty()) {
            return 0;
        }

        long now = System.currentTimeMillis();
        Long oldestInWindow = timestamps.peekFirst();
        if (oldestInWindow == null) {
            return 0;
        }

        long elapsed = now - oldestInWindow;
        if (elapsed >= ONE_MINUTE_MS) {
            return 0;
        }

        return (int) ((ONE_MINUTE_MS - elapsed + 999) / 1000);
    }
}
