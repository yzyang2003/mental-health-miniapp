package com.example.demo.service.music;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.MusicPlaylistVO;

/**
 * 音乐疗愈歌单服务接口。
 */
public interface MusicPlaylistService {

    /**
     * 分页获取歌单列表。
     *
     * @param page        当前页
     * @param size        每页大小
     * @param emotionType 情绪类型筛选
     * @return 分页结果
     */
    Page<MusicPlaylistVO> getPlaylistPage(int page, int size, String emotionType);

    /**
     * 统计播放次数并返回最新数据。
     *
     * @param id 歌单 ID
     * @return 最新歌单信息
     */
    MusicPlaylistVO playPlaylist(Long id);
}
