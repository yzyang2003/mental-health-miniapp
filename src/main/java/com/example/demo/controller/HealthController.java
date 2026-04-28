package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 健康检查接口。
 */
@Tag(name = "Health", description = "Project health check APIs")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "Application health check")
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "message", "Spring Boot 3.x project started successfully"
        );
    }
}
