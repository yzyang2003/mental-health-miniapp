package com.example.demo.service.article.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.ArticleVO;
import com.example.demo.entity.Article;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ArticleMapper;
import com.example.demo.service.article.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 心理文章服务实现。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private static final int STATUS_NORMAL = 1;

    private final ArticleMapper articleMapper;

    @Override
    public Page<ArticleVO> getArticlePage(int page, int size, String tag) {
        long current = Math.max(page, 1);
        long pageSize = Math.max(size, 1);

        long total = articleMapper.selectCount(buildQueryWrapper(tag));
        Page<ArticleVO> result = new Page<>(current, pageSize, total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        List<ArticleVO> records = articleMapper.selectList(
                        buildQueryWrapper(tag)
                                .orderByDesc(Article::getViewCount)
                                .orderByDesc(Article::getCreateTime)
                                .orderByDesc(Article::getId)
                                .last("limit " + offset + "," + pageSize)
                ).stream()
                .map(this::toArticleVO)
                .toList();
        result.setRecords(records);
        return result;
    }

    @Override
    public ArticleVO getArticleDetail(Long id) {
        if (id == null) {
            throw new BusinessException("文章 ID 不能为空");
        }

        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() == null || article.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("文章不存在或已下架");
        }
        Article updateView = new Article();
        updateView.setId(article.getId());
        updateView.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
        articleMapper.updateById(updateView);
        article.setViewCount(updateView.getViewCount());
        return toArticleVO(article);
    }

    private LambdaQueryWrapper<Article> buildQueryWrapper(String tag) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, STATUS_NORMAL);
        if (StringUtils.hasText(tag)) {
            queryWrapper.apply("JSON_CONTAINS(tags, JSON_QUOTE({0}))", tag.trim());
        }
        return queryWrapper;
    }

    private ArticleVO toArticleVO(Article article) {
        ArticleVO articleVO = new ArticleVO();
        articleVO.setId(article.getId());
        articleVO.setTitle(article.getTitle());
        articleVO.setCover(article.getCover());
        articleVO.setSummary(article.getSummary());
        articleVO.setContent(article.getContent());
        articleVO.setContentUrl(article.getContentUrl());
        articleVO.setTags(article.getTags() == null ? Collections.emptyList() : article.getTags());
        articleVO.setViewCount(article.getViewCount() == null ? 0 : article.getViewCount());
        articleVO.setCreateTime(article.getCreateTime());
        articleVO.setUpdateTime(article.getUpdateTime());
        return articleVO;
    }
}
