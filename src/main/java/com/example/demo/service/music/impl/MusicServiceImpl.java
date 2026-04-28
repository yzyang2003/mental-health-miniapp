package com.example.demo.service.music.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.MusicVO;
import com.example.demo.entity.Music;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.MusicMapper;
import com.example.demo.service.music.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 音乐疗愈服务实现。
 */
@Service
@RequiredArgsConstructor
public class MusicServiceImpl implements MusicService {

    private static final int STATUS_NORMAL = 1;

    private final MusicMapper musicMapper;

    @Override
    public Page<MusicVO> getMusicPage(int page, int size, String emotionType) {
        long current = Math.max(page, 1);
        long pageSize = Math.max(size, 1);

        long total = musicMapper.selectCount(buildQueryWrapper(emotionType));
        Page<MusicVO> result = new Page<>(current, pageSize, total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        List<MusicVO> records = musicMapper.selectList(
                        buildQueryWrapper(emotionType)
                                .orderByDesc(Music::getPlayCount)
                                .orderByDesc(Music::getCreateTime)
                                .orderByDesc(Music::getId)
                                .last("limit " + offset + "," + pageSize)
                ).stream()
                .map(this::toMusicVO)
                .toList();
        result.setRecords(records);
        return result;
    }

    @Override
    public MusicVO playMusic(Long id) {
        if (id == null) {
            throw new BusinessException("音乐 ID 不能为空");
        }

        Music music = musicMapper.selectById(id);
        if (music == null || music.getStatus() == null || music.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("音乐不存在或已下架");
        }

        music.setPlayCount((music.getPlayCount() == null ? 0 : music.getPlayCount()) + 1);
        musicMapper.updateById(music);
        return toMusicVO(musicMapper.selectById(id));
    }

    private LambdaQueryWrapper<Music> buildQueryWrapper(String emotionType) {
        LambdaQueryWrapper<Music> queryWrapper = new LambdaQueryWrapper<Music>()
                .eq(Music::getStatus, STATUS_NORMAL);
        if (StringUtils.hasText(emotionType)) {
            queryWrapper.eq(Music::getEmotionType, emotionType.trim());
        }
        return queryWrapper;
    }

    private MusicVO toMusicVO(Music music) {
        MusicVO musicVO = new MusicVO();
        musicVO.setId(music.getId());
        musicVO.setSongName(music.getSongName());
        musicVO.setSinger(music.getSinger());
        musicVO.setCover(music.getCover());
        musicVO.setEmotionType(music.getEmotionType());
        musicVO.setUrl(music.getUrl());
        musicVO.setDuration(music.getDuration());
        musicVO.setPlayCount(music.getPlayCount() == null ? 0 : music.getPlayCount());
        musicVO.setCreateTime(music.getCreateTime());
        return musicVO;
    }
}
