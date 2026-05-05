package com.example.demo.controller.station;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.MusicPlaylistVO;
import com.example.demo.service.music.MusicPlaylistService;
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
 * 音乐疗愈歌单控制器。
 */
@Tag(name = "MusicPlaylist", description = "Music Playlist APIs")
@RestController
@RequestMapping("/api/music/playlist")
@RequiredArgsConstructor
public class MusicPlaylistController {

    private final MusicPlaylistService musicPlaylistService;

    @Operation(summary = "分页获取音乐疗愈歌单列表")
    @GetMapping("/list")
    public Page<MusicPlaylistVO> getPlaylistPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String emotionType
    ) {
        return musicPlaylistService.getPlaylistPage(page, size, emotionType);
    }

    @Operation(summary = "统计歌单播放次数")
    @PostMapping("/play/{id}")
    public MusicPlaylistVO playPlaylist(@PathVariable Long id) {
        return musicPlaylistService.playPlaylist(id);
    }
}
