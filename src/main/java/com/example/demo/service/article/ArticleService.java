package com.example.demo.service.article;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.ArticleVO;

/**
 * 心理文章服务接口。
 */
public interface ArticleService {

    /**
     * 分页获取文章列表。
     *
     * @param page 当前页
     * @param size 每页大小
     * @param tag  标签筛选
     * @return 分页结果
     */
    Page<ArticleVO> getArticlePage(int page, int size, String tag);

    /**
     * 获取文章详情。
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    ArticleVO getArticleDetail(Long id);
}
