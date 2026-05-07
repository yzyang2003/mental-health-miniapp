package com.example.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Notice;

/**
 * 公告服务接口。
 */
public interface NoticeService {

    /**
     * 分页查询公告列表。
     *
     * @param page   页码
     * @param size   每页大小
     * @param status 状态筛选（可选）
     * @return 分页结果
     */
    Page<Notice> listNotices(int page, int size, Integer status);

    /**
     * 根据 ID 获取公告详情。
     *
     * @param id 公告 ID
     * @return 公告信息
     */
    Notice getNoticeById(Long id);

    /**
     * 创建公告。
     *
     * @param notice 公告信息
     * @return 创建后的公告（含 ID）
     */
    Notice createNotice(Notice notice);

    /**
     * 更新公告。
     *
     * @param notice 公告信息
     */
    void updateNotice(Notice notice);

    /**
     * 删除公告。
     *
     * @param id 公告 ID
     */
    void deleteNotice(Long id);
}
