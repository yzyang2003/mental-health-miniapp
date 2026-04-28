package com.example.demo.service.selfhealing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.SelfHealingVO;
import com.example.demo.entity.SelfHealing;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.SelfHealingMapper;
import com.example.demo.service.selfhealing.SelfHealingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 自我疗愈服务实现。
 */
@Service
@RequiredArgsConstructor
public class SelfHealingServiceImpl implements SelfHealingService {

    private static final int STATUS_NORMAL = 1;

    private final SelfHealingMapper selfHealingMapper;

    @Override
    public Page<SelfHealingVO> getSelfHealingPage(int page, int size, String issueType) {
        long current = Math.max(page, 1);
        long pageSize = Math.max(size, 1);

        long total = selfHealingMapper.selectCount(buildQueryWrapper(issueType));
        Page<SelfHealingVO> result = new Page<>(current, pageSize, total);
        if (total == 0) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        long offset = (current - 1) * pageSize;
        List<SelfHealingVO> records = selfHealingMapper.selectList(
                        buildQueryWrapper(issueType)
                                .orderByDesc(SelfHealing::getCreateTime)
                                .orderByDesc(SelfHealing::getId)
                                .last("limit " + offset + "," + pageSize)
                ).stream()
                .map(this::toSelfHealingVO)
                .toList();
        result.setRecords(records);
        return result;
    }

    @Override
    public SelfHealingVO getSelfHealingDetail(Long id) {
        if (id == null) {
            throw new BusinessException("自我疗愈 ID 不能为空");
        }

        SelfHealing selfHealing = selfHealingMapper.selectById(id);
        if (selfHealing == null || selfHealing.getStatus() == null || selfHealing.getStatus() != STATUS_NORMAL) {
            throw new BusinessException("自我疗愈内容不存在或已下架");
        }
        return toSelfHealingVO(selfHealing);
    }

    private LambdaQueryWrapper<SelfHealing> buildQueryWrapper(String issueType) {
        LambdaQueryWrapper<SelfHealing> queryWrapper = new LambdaQueryWrapper<SelfHealing>()
                .eq(SelfHealing::getStatus, STATUS_NORMAL);
        if (StringUtils.hasText(issueType)) {
            queryWrapper.eq(SelfHealing::getIssueType, issueType.trim());
        }
        return queryWrapper;
    }

    private SelfHealingVO toSelfHealingVO(SelfHealing selfHealing) {
        SelfHealingVO selfHealingVO = new SelfHealingVO();
        selfHealingVO.setId(selfHealing.getId());
        selfHealingVO.setTitle(selfHealing.getTitle());
        selfHealingVO.setCover(selfHealing.getCover());
        selfHealingVO.setDescription(selfHealing.getDescription());
        selfHealingVO.setIssueType(selfHealing.getIssueType());
        selfHealingVO.setSteps(selfHealing.getSteps() == null ? Collections.emptyList() : selfHealing.getSteps());
        selfHealingVO.setDuration(selfHealing.getDuration());
        selfHealingVO.setCreateTime(selfHealing.getCreateTime());
        selfHealingVO.setUpdateTime(selfHealing.getUpdateTime());
        return selfHealingVO;
    }
}
