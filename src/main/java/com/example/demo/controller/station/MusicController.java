package com.example.demo.controller.station;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.MusicVO;
import com.example.demo.service.music.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 音乐疗愈控制器。
 */
@Tag(name = "Music", description = "Music APIs")
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @Operation(summary = "分页获取音乐疗愈列表")
    @GetMapping("/list")
    public Page<MusicVO> getMusicPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String emotionType
    ) {
        return musicService.getMusicPage(page, size, emotionType);
    }

    @Operation(summary = "统计音乐播放次数")
    @PostMapping("/play/{id}")
    public MusicVO playMusic(@PathVariable Long id) {
        return musicService.playMusic(id);
    }
}
