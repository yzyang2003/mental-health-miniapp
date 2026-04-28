package com.example.demo.controller.station;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.SelfHealingVO;
import com.example.demo.service.selfhealing.SelfHealingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自我疗愈控制器。
 */
@Tag(name = "SelfHealing", description = "Self healing APIs")
@RestController
@RequestMapping("/api/selfHealing")
@RequiredArgsConstructor
public class SelfHealingController {

    private final SelfHealingService selfHealingService;

    @Operation(summary = "分页获取自我疗愈列表")
    @GetMapping("/list")
    public Page<SelfHealingVO> getSelfHealingPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String issueType
    ) {
        return selfHealingService.getSelfHealingPage(page, size, issueType);
    }

    @Operation(summary = "获取自我疗愈详情")
    @GetMapping("/{id}")
    public SelfHealingVO getSelfHealingDetail(@PathVariable Long id) {
        return selfHealingService.getSelfHealingDetail(id);
    }
}
