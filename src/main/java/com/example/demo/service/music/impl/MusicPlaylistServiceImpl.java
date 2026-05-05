package com.example.demo.service.music.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.MusicPlaylistVO;
import com.example.demo.entity.MusicPlaylist;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.MusicPlaylistMapper;
import com.example.demo.service.music.MusicPlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 音乐疗愈歌单服务实现。
 */
@Service
@RequiredArgsConstructor
public class MusicPlaylistServiceImpl implements MusicPlaylistService {

    private static final int STATUS_NORMAL = 1;

    private final MusicPlaylistMapper musicPlaylistMapper;

    @Override
    public Page<MusicPlaylistVO> getPlaylistPage(int page, int size, String emotionType) {
        long current = Math.max(page, 1);
        long pageSize = Math.max(size, 1);

        long total = musicPlaylistMapper.selectCount(buildQueryWrapper(emotionType));
        Page<MusicPlaylistVO> result = new Page<>(current, pageSize, total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        List<MusicPlaylistVO> records = musicPlaylistMapper.selectList(
                buildQueryWrapper(emotionType)
                        .orderByDesc(MusicPlaylist::getPlayCount)
                        .orderByAsc(MusicPlaylist::getId)
                        .last("limit " + offset + "," + pageSize)
        ).stream()
                .map(this::toMusicPlaylistVO)
                .toList();
        result.setRecords(records);
        return result;
    }

    @Override
    public MusicPlaylistVO playPlaylist(Long id) {
        if (id == null) {
            throw new BusinessException("歌单 ID 不能为空");
        }

        MusicPlaylist playlist = musicPlaylistMapper.selectById(id);
        if (playlist == null || playlist.getStatus() == null || playlist.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("歌单不存在或已下架");
        }

        playlist.setPlayCount((playlist.getPlayCount() == null ? 0 : playlist.getPlayCount()) + 1);
        musicPlaylistMapper.updateById(playlist);
        return toMusicPlaylistVO(musicPlaylistMapper.selectById(id));
    }

    private LambdaQueryWrapper<MusicPlaylist> buildQueryWrapper(String emotionType) {
        LambdaQueryWrapper<MusicPlaylist> queryWrapper = new LambdaQueryWrapper<MusicPlaylist>()
                .eq(MusicPlaylist::getStatus, STATUS_NORMAL);
        if (StringUtils.hasText(emotionType)) {
            queryWrapper.eq(MusicPlaylist::getEmotionType, emotionType.trim());
        }
        return queryWrapper;
    }

    private MusicPlaylistVO toMusicPlaylistVO(MusicPlaylist playlist) {
        MusicPlaylistVO vo = new MusicPlaylistVO();
        vo.setId(playlist.getId());
        vo.setName(playlist.getName());
        vo.setDescription(playlist.getDescription());
        vo.setCoverImage(playlist.getCoverImage());
        vo.setShareUrl(playlist.getShareUrl());
        vo.setEmotionType(playlist.getEmotionType());
        vo.setDurationText(playlist.getDurationText());
        vo.setPlayCount(playlist.getPlayCount() == null ? 0 : playlist.getPlayCount());
        vo.setCreateTime(playlist.getCreateTime());
        return vo;
    }
}
