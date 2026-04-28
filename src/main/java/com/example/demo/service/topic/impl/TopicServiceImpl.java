package com.example.demo.service.topic.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.PublishTopicRequest;
import com.example.demo.dto.TopicVO;
import com.example.demo.entity.Reply;
import com.example.demo.entity.Topic;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.ReplyMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.service.UserService;
import com.example.demo.service.topic.TopicService;
import com.example.demo.utils.SensitiveWordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 树洞帖子服务实现。
 */
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 2;
    private static final String ANONYMOUS_NAME = "匿名用户";
    private static final String DEFAULT_PUBLISHER_NAME = "微信用户";
    private static final String UPLOADS_SEGMENT = "/uploads/";

    private final TopicMapper topicMapper;
    private final ReplyMapper replyMapper;
    private final UserService userService;

    @Override
    public TopicVO publishTopic(String openid, PublishTopicRequest request) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (request == null || !StringUtils.hasText(request.getContent())) {
            throw new BusinessException("帖子内容不能为空");
        }
        if (SensitiveWordUtil.containsSensitiveWord(request.getContent())) {
            throw new BusinessException("帖子内容包含敏感词，请修改后重试");
        }

        Topic topic = new Topic();
        topic.setContent(request.getContent().trim());
        topic.setImages(request.getImages() == null ? Collections.emptyList() : request.getImages());
        topic.setPublisherOpenid(openid);
        topic.setAnonymous(!Boolean.FALSE.equals(request.getAnonymous()));
        topic.setStatus(STATUS_NORMAL);
        topicMapper.insert(topic);

        Topic savedTopic = topicMapper.selectById(topic.getId());
        return toTopicVO(savedTopic, openid, null);
    }

    @Override
    public Page<TopicVO> getTopicPage(String currentOpenid, int page, int size, boolean mineOnly) {
        long current = Math.max(page, 1);
        long pageSize = Math.max(size, 1);

        LambdaQueryWrapper<Topic> countWrapper = new LambdaQueryWrapper<Topic>()
                .eq(Topic::getStatus, STATUS_NORMAL);
        if (mineOnly) {
            countWrapper.eq(Topic::getPublisherOpenid, currentOpenid);
        }
        long total = topicMapper.selectCount(countWrapper);

        Page<TopicVO> result = new Page<>(current, pageSize, total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        LambdaQueryWrapper<Topic> queryWrapper = new LambdaQueryWrapper<Topic>()
                .eq(Topic::getStatus, STATUS_NORMAL);
        if (mineOnly) {
            queryWrapper.eq(Topic::getPublisherOpenid, currentOpenid);
        }
        queryWrapper
                .orderByDesc(Topic::getCreateTime)
                .last("limit " + offset + "," + pageSize);

        List<Topic> topics = topicMapper.selectList(queryWrapper);

        Map<Long, Long> replyCountMap = buildReplyCountMap(topics);
        List<TopicVO> records = topics.stream()
                .map(topic -> toTopicVO(topic, currentOpenid, replyCountMap))
                .toList();
        result.setRecords(records);
        return result;
    }

    @Override
    public TopicVO getTopicDetail(String currentOpenid, Long topicId) {
        if (topicId == null) {
            throw new BusinessException("topicId 不能为空");
        }

        Topic topic = topicMapper.selectById(topicId);
        if (topic == null || topic.getStatus() == null || topic.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("帖子不存在或已删除");
        }
        return toTopicVO(topic, currentOpenid, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(String openid, Long topicId) {
        if (!StringUtils.hasText(openid)) {
            throw new BusinessException("openid 不能为空");
        }
        if (topicId == null) {
            throw new BusinessException("topicId 不能为空");
        }

        Topic topic = topicMapper.selectById(topicId);
        if (topic == null || topic.getStatus() == null || topic.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("帖子不存在或已删除");
        }
        if (!openid.equals(topic.getPublisherOpenid())) {
            throw new BusinessException("仅可删除自己发布的帖子");
        }

        Topic deleting = new Topic();
        deleting.setId(topicId);
        deleting.setStatus(STATUS_DELETED);
        deleting.setContent("");
        deleting.setImages(Collections.emptyList());
        topicMapper.updateById(deleting);

        Reply deletingReply = new Reply();
        deletingReply.setStatus(STATUS_DELETED);
        deletingReply.setContent("");
        deletingReply.setRepliedUserOpenid(null);
        deletingReply.setRepliedReplyId(null);
        replyMapper.update(
                deletingReply,
                new LambdaQueryWrapper<Reply>()
                        .eq(Reply::getTopicId, topicId)
                        .eq(Reply::getStatus, STATUS_NORMAL)
        );

        deleteLocalUploadFiles(topic.getImages());
    }

    private TopicVO toTopicVO(Topic topic, String currentOpenid, Map<Long, Long> replyCountMap) {
        TopicVO topicVO = new TopicVO();
        topicVO.setId(topic.getId());
        topicVO.setContent(topic.getContent());
        topicVO.setImages(topic.getImages());
        topicVO.setAnonymous(topic.getAnonymous());
        topicVO.setCreateTime(topic.getCreateTime());
        topicVO.setPublisherName(resolvePublisherName(topic));
        topicVO.setCanDelete(StringUtils.hasText(currentOpenid) && currentOpenid.equals(topic.getPublisherOpenid()));
        long replyCount;
        if (replyCountMap != null) {
            replyCount = replyCountMap.getOrDefault(topic.getId(), 0L);
        } else {
            replyCount = replyMapper.selectCount(
                    new LambdaQueryWrapper<Reply>()
                            .eq(Reply::getTopicId, topic.getId())
                            .eq(Reply::getStatus, STATUS_NORMAL)
            );
        }
        topicVO.setReplyCount(replyCount);
        return topicVO;
    }

    private Map<Long, Long> buildReplyCountMap(List<Topic> topics) {
        if (topics == null || topics.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> topicIds = topics.stream()
                .map(Topic::getId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (topicIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Map<String, Object>> rows = replyMapper.selectMaps(
                new QueryWrapper<Reply>()
                        .select("topic_id AS topicId", "COUNT(*) AS replyCount")
                        .in("topic_id", topicIds)
                        .eq("status", STATUS_NORMAL)
                        .groupBy("topic_id")
        );
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        return rows.stream()
                .filter(row -> row.get("topicId") != null)
                .collect(Collectors.toMap(
                row -> Long.valueOf(String.valueOf(row.get("topicId"))),
                row -> {
                    Object replyCount = row.get("replyCount");
                    return replyCount == null ? 0L : Long.valueOf(String.valueOf(replyCount));
                },
                (left, right) -> right
        ));
    }

    private String resolvePublisherName(Topic topic) {
        if (Boolean.TRUE.equals(topic.getAnonymous())) {
            return ANONYMOUS_NAME;
        }

        String nickname = userService.getNicknameByOpenid(topic.getPublisherOpenid());
        if (!StringUtils.hasText(nickname)) {
            return DEFAULT_PUBLISHER_NAME;
        }
        return nickname;
    }

    private void deleteLocalUploadFiles(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        Path uploadsRoot = Paths.get("./uploads").toAbsolutePath().normalize();
        for (String url : imageUrls) {
            String relativePath = resolveRelativeUploadPath(url);
            boolean deleted = deleteByRelativePath(uploadsRoot, relativePath);
            if (deleted) {
                continue;
            }
            deleteByFilenameFallback(uploadsRoot, url);
        }
    }

    private String resolveRelativeUploadPath(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String normalized = normalizeUploadUrl(url);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        int idx = normalized.toLowerCase().indexOf(UPLOADS_SEGMENT);
        if (idx < 0) {
            if (normalized.startsWith("uploads/")) {
                return normalized.substring("uploads/".length());
            }
            return null;
        }
        return normalized.substring(idx + UPLOADS_SEGMENT.length());
    }

    private String normalizeUploadUrl(String url) {
        String normalized = url.replace("\\", "/").trim();
        int queryIdx = normalized.indexOf('?');
        if (queryIdx >= 0) {
            normalized = normalized.substring(0, queryIdx);
        }
        int hashIdx = normalized.indexOf('#');
        if (hashIdx >= 0) {
            normalized = normalized.substring(0, hashIdx);
        }
        // 兼容 URL 编码路径（如空格、中文）
        normalized = URLDecoder.decode(normalized, StandardCharsets.UTF_8);
        return normalized;
    }

    private boolean deleteByRelativePath(Path uploadsRoot, String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            return false;
        }
        Path target = uploadsRoot.resolve(relativePath).normalize();
        if (!target.startsWith(uploadsRoot)) {
            return false;
        }
        try {
            if (Files.deleteIfExists(target)) {
                cleanupEmptyParentDirs(target.getParent(), uploadsRoot);
                return true;
            }
        } catch (IOException ignored) {
            // 删除失败不影响删帖主流程
        }
        return false;
    }

    private void deleteByFilenameFallback(Path uploadsRoot, String rawUrl) {
        if (!StringUtils.hasText(rawUrl)) {
            return;
        }
        String normalized = normalizeUploadUrl(rawUrl);
        if (!StringUtils.hasText(normalized)) {
            return;
        }
        String fileName = extractFileName(normalized);
        if (!StringUtils.hasText(fileName)) {
            return;
        }
        try (var stream = Files.walk(uploadsRoot)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> fileName.equals(path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            cleanupEmptyParentDirs(path.getParent(), uploadsRoot);
                        } catch (IOException ignored) {
                            // ignore
                        }
                    });
        } catch (IOException ignored) {
            // ignore
        }
    }

    private String extractFileName(String normalizedUrl) {
        int slashIdx = normalizedUrl.lastIndexOf('/');
        if (slashIdx < 0 || slashIdx >= normalizedUrl.length() - 1) {
            return null;
        }
        String fileName = normalizedUrl.substring(slashIdx + 1).trim();
        if (!StringUtils.hasText(fileName) || fileName.contains("..")) {
            return null;
        }
        return fileName;
    }

    private void cleanupEmptyParentDirs(Path dir, Path uploadsRoot) {
        Path current = dir;
        while (current != null && current.startsWith(uploadsRoot) && !current.equals(uploadsRoot)) {
            try {
                if (!Files.exists(current) || !Files.isDirectory(current)) {
                    break;
                }
                try (var stream = Files.list(current)) {
                    if (stream.findAny().isPresent()) {
                        break;
                    }
                }
                Files.deleteIfExists(current);
                current = current.getParent();
            } catch (IOException ignored) {
                break;
            }
        }
    }
}
