package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.SelfHealing;
import com.example.demo.mapper.SelfHealingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 自愈练习管理控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/healing")
@RequiredArgsConstructor
public class AdminHealingController {

    private final SelfHealingMapper selfHealingMapper;

    @GetMapping("/list")
    public Result<Page<SelfHealing>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SelfHealing> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SelfHealing> wrapper = new LambdaQueryWrapper<SelfHealing>()
                .orderByDesc(SelfHealing::getCreateTime);
        Page<SelfHealing> result = selfHealingMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<Map<String, Long>> create(@RequestBody SelfHealing healing) {
        healing.setCreateTime(LocalDateTime.now());
        healing.setUpdateTime(LocalDateTime.now());
        selfHealingMapper.insert(healing);
        return Result.success(Map.of("id", healing.getId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SelfHealing healing) {
        healing.setId(id);
        healing.setUpdateTime(LocalDateTime.now());
        selfHealingMapper.updateById(healing);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        selfHealingMapper.deleteById(id);
        return Result.success(null);
    }
}
