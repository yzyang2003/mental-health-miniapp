package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Music;
import com.example.demo.mapper.MusicMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 音乐管理控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/music")
@RequiredArgsConstructor
public class AdminMusicController {

    private final MusicMapper musicMapper;

    @GetMapping("/list")
    public Result<Page<Music>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Music> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Music> wrapper = new LambdaQueryWrapper<Music>()
                .orderByDesc(Music::getCreateTime);
        Page<Music> result = musicMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    @PostMapping
    public Result<Map<String, Long>> create(@RequestBody Music music) {
        musicMapper.insert(music);
        return Result.success(Map.of("id", music.getId()));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Music music) {
        music.setId(id);
        musicMapper.updateById(music);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        musicMapper.deleteById(id);
        return Result.success(null);
    }
}
