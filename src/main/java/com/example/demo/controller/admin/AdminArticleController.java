package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Article;
import com.example.demo.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文章管理控制器（管理后台）。
 */
@RestController
@RequestMapping("/api/admin/article")
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleMapper articleMapper;

    /**
     * 文章分页列表。
     */
    @GetMapping("/list")
    public Result<Page<Article>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        Page<Article> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Article::getStatus, status);
        }
        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> result = articleMapper.selectPage(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 文章详情。
     */
    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            return Result.error(404, "文章不存在");
        }
        return Result.success(article);
    }

    /**
     * 新建文章。
     */
    @PostMapping
    public Result<Map<String, Long>> create(@RequestBody Article article) {
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        articleMapper.insert(article);
        return Result.success(Map.of("id", article.getId()));
    }

    /**
     * 编辑文章。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Article article) {
        Article existing = articleMapper.selectById(id);
        if (existing == null) {
            return Result.error(404, "文章不存在");
        }
        article.setId(id);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        return Result.success(null);
    }

    /**
     * 修改文章状态（上/下架）。
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.error(400, "状态不能为空");
        }
        Article article = new Article();
        article.setId(id);
        article.setStatus(status);
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        return Result.success(null);
    }

    /**
     * 删除文章（软删除）。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(2); // 软删除
        article.setUpdateTime(LocalDateTime.now());
        articleMapper.updateById(article);
        return Result.success(null);
    }

    /**
     * AI 填充：根据网页链接抓取标题和正文。
     */
    @PostMapping("/ai-fill")
    public Result<Map<String, String>> aiFill(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (!StringUtils.hasText(url)) {
            return Result.error(400, "链接不能为空");
        }
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            String title = doc.title();
            // Extract main content — try <article>, <main>, then fall back to <body>
            org.jsoup.nodes.Element contentEl = doc.selectFirst("article");
            if (contentEl == null) contentEl = doc.selectFirst("main");
            if (contentEl == null) contentEl = doc.body();

            String content = contentEl != null ? contentEl.html() : "";
            // Generate summary from text content (first 200 chars)
            String textContent = contentEl != null ? contentEl.text() : "";
            String summary = textContent.length() > 200 ? textContent.substring(0, 200) + "..." : textContent;

            Map<String, String> result = new java.util.HashMap<>();
            result.put("title", title);
            result.put("content", content);
            result.put("summary", summary);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "抓取网页失败: " + e.getMessage());
        }
    }
}
