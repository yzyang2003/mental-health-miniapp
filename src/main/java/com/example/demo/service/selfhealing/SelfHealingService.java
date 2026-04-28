package com.example.demo.service.selfhealing;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.SelfHealingVO;

/**
 * 自我疗愈服务接口。
 */
public interface SelfHealingService {

    /**
     * 分页获取自我疗愈列表。
     *
     * @param page      当前页
     * @param size      每页大小
     * @param issueType 问题类型筛选
     * @return 分页结果
     */
    Page<SelfHealingVO> getSelfHealingPage(int page, int size, String issueType);

    /**
     * 获取自我疗愈详情。
     *
     * @param id 自我疗愈 ID
     * @return 详情信息
     */
    SelfHealingVO getSelfHealingDetail(Long id);
}
