package com.example.demo.controller.station;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.ArticleVO;
import com.example.demo.service.article.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 心理文章控制器。
 */
@Tag(name = "Article", description = "Article APIs")
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "分页获取心理文章列表")
    @GetMapping("/list")
    public Page<ArticleVO> getArticlePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag
    ) {
        return articleService.getArticlePage(page, size, tag);
    }

    @Operation(summary = "获取心理文章详情")
    @GetMapping("/{id}")
    public ArticleVO getArticleDetail(@PathVariable Long id) {
        return articleService.getArticleDetail(id);
    }
}
