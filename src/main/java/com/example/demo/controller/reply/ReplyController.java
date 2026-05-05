package com.example.demo.controller.reply;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PublishReplyRequest;
import com.example.demo.dto.ReplyVO;
import com.example.demo.service.reply.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 树洞回复控制器。
 */
@Tag(name = "Reply", description = "Reply APIs")
@RestController
@RequestMapping("/api/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @Operation(summary = "发布回复")
    @PostMapping("/publish")
    public ReplyVO publishReply(@RequestBody PublishReplyRequest request, HttpServletRequest httpServletRequest) {
        Object openidAttr = httpServletRequest.getAttribute("openid");
        if (!(openidAttr instanceof String openid)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }

        return replyService.publishReply(openid, request);
    }

    @Operation(summary = "分页获取帖子回复（加载更多）")
    @GetMapping("/list")
    public Page<ReplyVO> getReplyPage(
            @RequestParam Long topicId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpServletRequest
    ) {
        String currentOpenid = resolveOptionalOpenid(httpServletRequest);
        return replyService.getRepliesByTopicId(currentOpenid, topicId, page, size);
    }

    @Operation(summary = "删除回复（级联删除其子回复）")
    @DeleteMapping("/{replyId}")
    public void deleteReply(@PathVariable Long replyId, HttpServletRequest httpServletRequest) {
        Object openidAttr = httpServletRequest.getAttribute("openid");
        if (!(openidAttr instanceof String openid)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }
        replyService.deleteReply(openid, replyId);
    }

    private String resolveOptionalOpenid(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (openidAttr instanceof String openid && !openid.isBlank()) {
            return openid;
        }
        return null;
    }
}