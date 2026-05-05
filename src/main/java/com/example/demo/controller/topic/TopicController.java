package com.example.demo.controller.topic;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PublishTopicRequest;
import com.example.demo.dto.ReplyVO;
import com.example.demo.dto.TopicDetailVO;
import com.example.demo.dto.TopicVO;
import com.example.demo.service.reply.ReplyService;
import com.example.demo.service.topic.TopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 树洞帖子控制器。
 */
@Tag(name = "Topic", description = "Tree hole topic APIs")
@RestController
@RequestMapping("/api/topic")
@RequiredArgsConstructor
public class TopicController {

    private static final long MAX_IMAGE_SIZE_BYTES = 8L * 1024L * 1024L;
    private static final DateTimeFormatter DIR_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TopicService topicService;
    private final ReplyService replyService;

    @Operation(summary = "发布树洞帖子")
    @PostMapping("/publish")
    public TopicVO publishTopic(@RequestBody PublishTopicRequest request, HttpServletRequest httpServletRequest) {
        Object openidAttr = httpServletRequest.getAttribute("openid");
        if (!(openidAttr instanceof String openid)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }

        return topicService.publishTopic(openid, request);
    }

    @Operation(summary = "分页获取树洞帖子列表（mine=true 仅返回当前用户发帖，需要登录）")
    @GetMapping("/list")
    public Page<TopicVO> getTopicPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean mine,
            HttpServletRequest httpServletRequest
    ) {
        String currentOpenid = mine ? requireLoginOpenid(httpServletRequest) : resolveOptionalOpenid(httpServletRequest);
        return topicService.getTopicPage(currentOpenid, page, size, mine);
    }

    @Operation(summary = "获取帖子详情和回复列表")
    @GetMapping("/{topicId}/detail")
    public TopicDetailVO getTopicDetail(@PathVariable Long topicId, HttpServletRequest httpServletRequest) {
        String currentOpenid = resolveOptionalOpenid(httpServletRequest);
        TopicVO topic = topicService.getTopicDetail(currentOpenid, topicId);
        Page<ReplyVO> replyPage = replyService.getRepliesByTopicId(currentOpenid, topicId, 1, 50);

        TopicDetailVO topicDetailVO = new TopicDetailVO();
        topicDetailVO.setTopic(topic);
        topicDetailVO.setReplies(replyPage.getRecords());
        topicDetailVO.setReplyTotal(replyPage.getTotal());
        topicDetailVO.setReplyPages(replyPage.getPages());
        topicDetailVO.setReplyCurrent(replyPage.getCurrent());
        return topicDetailVO;
    }

    @Operation(summary = "删除帖子（级联删除帖子内回复）")
    @DeleteMapping("/{topicId}")
    public void deleteTopic(@PathVariable Long topicId, HttpServletRequest httpServletRequest) {
        String openid = requireLoginOpenid(httpServletRequest);
        topicService.deleteTopic(openid, topicId);
    }

    @Operation(summary = "上传树洞图片")
    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadTopicImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest
    ) {
        requireLoginOpenid(httpServletRequest);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择图片文件");
        }
        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "图片不能超过 8MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "仅支持图片文件");
        }

        String extension = resolveExtension(file);
        Path rootDir = Paths.get("./uploads/topic").toAbsolutePath().normalize();
        Path dayDir = rootDir.resolve(LocalDate.now().format(DIR_DATE_FORMATTER));
        try {
            Files.createDirectories(dayDir);
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
            Path target = dayDir.resolve(fileName).normalize();
            if (!target.startsWith(rootDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法文件路径");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String relativePath = target.toString().replace("\\", "/");
            int index = relativePath.lastIndexOf("/uploads/");
            String publicPath = index >= 0 ? relativePath.substring(index) : "/uploads/topic/" + dayDir.getFileName() + "/" + fileName;
            return Map.of("url", publicPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "图片上传失败");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (StringUtils.hasText(originalName)) {
            int idx = originalName.lastIndexOf('.');
            if (idx >= 0 && idx < originalName.length() - 1) {
                String ext = originalName.substring(idx).toLowerCase();
                if (ext.matches("\\.[a-z0-9]{1,8}")) {
                    return ext;
                }
            }
        }
        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return ".gif";
        }
        return ".jpg";
    }

    private String resolveOptionalOpenid(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (openidAttr instanceof String openid && !openid.isBlank()) {
            return openid;
        }
        return null;
    }

    private String requireLoginOpenid(HttpServletRequest request) {
        Object openidAttr = request.getAttribute("openid");
        if (!(openidAttr instanceof String openid) || openid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或登录已失效");
        }
        return openid;
    }
}
