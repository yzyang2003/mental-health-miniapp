package com.example.demo.service.music;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.MusicVO;

/**
 * 音乐疗愈服务接口。
 */
public interface MusicService {

    /**
     * 分页获取音乐列表。
     *
     * @param page        当前页
     * @param size        每页大小
     * @param emotionType 情绪类型筛选
     * @return 分页结果
     */
    Page<MusicVO> getMusicPage(int page, int size, String emotionType);

    /**
     * 统计播放次数并返回最新数据。
     *
     * @param id 音乐 ID
     * @return 最新音乐信息
     */
    MusicVO playMusic(Long id);
}
