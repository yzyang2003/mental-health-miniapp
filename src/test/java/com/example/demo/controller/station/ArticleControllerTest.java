package com.example.demo.controller.station;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.ArticleVO;
import com.example.demo.service.article.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ArticleControllerTest {

    @Test
    void list_returnsOk() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        Page<ArticleVO> page = new Page<>(1, 10);
        when(articleService.getArticlePage(eq(1), eq(10), isNull())).thenReturn(page);

        ArticleController controller = new ArticleController(articleService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/article/list"))
                .andExpect(status().isOk());
    }

    @Test
    void detail_returnsOk() throws Exception {
        ArticleService articleService = mock(ArticleService.class);
        ArticleVO vo = new ArticleVO();
        vo.setId(1L);
        vo.setTitle("t");
        when(articleService.getArticleDetail(1L)).thenReturn(vo);

        ArticleController controller = new ArticleController(articleService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/article/1"))
                .andExpect(status().isOk());
    }
}
