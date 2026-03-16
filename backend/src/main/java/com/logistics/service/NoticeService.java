package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.logistics.common.BusinessException;
import com.logistics.entity.Notice;
import com.logistics.entity.NoticeAttachment;
import com.logistics.mapper.NoticeAttachmentMapper;
import com.logistics.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class NoticeService extends ServiceImpl<NoticeMapper, Notice> {

    private final NoticeAttachmentMapper attachmentMapper;
    private final FileService fileService;

    /** SSE 连接池 */
    private final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    // ========== 管理员操作 ==========

    /**
     * 发布公告
     */
    @Transactional
    public Notice addNotice(Notice notice, List<MultipartFile> files) {
        notice.setStatus(1);
        save(notice);

        // 上传附件
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                String path = fileService.upload(file, "notice");
                NoticeAttachment att = new NoticeAttachment();
                att.setNoticeId(notice.getNoticeId());
                att.setFileName(file.getOriginalFilename());
                att.setFilePath(path);
                att.setFileSize(file.getSize());
                att.setFileType(fileService.getFileType(file.getOriginalFilename()));
                attachmentMapper.insert(att);
            }
        }

        // SSE 推送新公告通知
        pushNotice(notice);
        return notice;
    }

    /**
     * 管理员查询公告列表
     */
    public Page<Notice> adminList(int page, int size, String title, String publisherName,
            String startTime, String endTime, Integer isTop) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1)
                .like(StringUtils.hasText(title), Notice::getTitle, title)
                .like(StringUtils.hasText(publisherName), Notice::getPublisherName, publisherName)
                .eq(isTop != null, Notice::getIsTop, isTop)
                .ge(StringUtils.hasText(startTime), Notice::getCreateTime, startTime)
                .le(StringUtils.hasText(endTime), Notice::getCreateTime, endTime)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 编辑公告
     */
    @Transactional
    public void updateNotice(Notice notice, List<MultipartFile> newFiles) {
        Notice existing = getById(notice.getNoticeId());
        if (existing == null || existing.getStatus() == 0) {
            throw new BusinessException("公告不存在");
        }
        Notice update = new Notice();
        update.setNoticeId(notice.getNoticeId());
        update.setTitle(notice.getTitle());
        update.setContent(notice.getContent());
        update.setIsTop(notice.getIsTop());
        updateById(update);

        // 新增附件
        if (newFiles != null && !newFiles.isEmpty()) {
            for (MultipartFile file : newFiles) {
                String path = fileService.upload(file, "notice");
                NoticeAttachment att = new NoticeAttachment();
                att.setNoticeId(notice.getNoticeId());
                att.setFileName(file.getOriginalFilename());
                att.setFilePath(path);
                att.setFileSize(file.getSize());
                att.setFileType(fileService.getFileType(file.getOriginalFilename()));
                attachmentMapper.insert(att);
            }
        }
    }

    /**
     * 删除公告（逻辑删除）
     */
    public void deleteNotice(Long noticeId) {
        Notice notice = getById(noticeId);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        Notice update = new Notice();
        update.setNoticeId(noticeId);
        update.setStatus(0);
        updateById(update);
    }

    /**
     * 置顶/取消置顶
     */
    public void toggleTop(Long noticeId, Integer isTop) {
        Notice update = new Notice();
        update.setNoticeId(noticeId);
        update.setIsTop(isTop);
        updateById(update);
    }

    // ========== 公共查询 ==========

    /**
     * 全角色查询公告列表
     */
    public Page<Notice> commonList(int page, int size, String title, String startTime, String endTime) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1)
                .like(StringUtils.hasText(title), Notice::getTitle, title)
                .ge(StringUtils.hasText(startTime), Notice::getCreateTime, startTime)
                .le(StringUtils.hasText(endTime), Notice::getCreateTime, endTime)
                .orderByDesc(Notice::getIsTop)
                .orderByDesc(Notice::getCreateTime);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * 查看公告详情 + 附件
     */
    public Notice getDetail(Long noticeId) {
        Notice notice = getById(noticeId);
        if (notice == null || notice.getStatus() == 0) {
            throw new BusinessException("公告不存在");
        }
        return notice;
    }

    /**
     * 根据公告ID查附件列表
     */
    public List<NoticeAttachment> getAttachments(Long noticeId) {
        return attachmentMapper.selectList(
                new LambdaQueryWrapper<NoticeAttachment>().eq(NoticeAttachment::getNoticeId, noticeId));
    }

    // ========== SSE 推送 ==========

    /**
     * 注册 SSE 连接
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(0L); // 不超时
        sseEmitters.put(userId, emitter);
        emitter.onCompletion(() -> sseEmitters.remove(userId));
        emitter.onTimeout(() -> sseEmitters.remove(userId));
        emitter.onError(e -> sseEmitters.remove(userId));
        return emitter;
    }

    /**
     * 推送新公告给所有在线用户
     */
    private void pushNotice(Notice notice) {
        List<Long> deadKeys = new CopyOnWriteArrayList<>();
        sseEmitters.forEach((userId, emitter) -> {
            try {
                Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("noticeId", notice.getNoticeId());
                payload.put("title", notice.getTitle() != null ? notice.getTitle() : "");
                payload.put("publisherName", notice.getPublisherName() != null ? notice.getPublisherName() : "");
                emitter.send(SseEmitter.event()
                        .name("notice")
                        .data(payload));
            } catch (Exception e) {
                deadKeys.add(userId);
            }
        });
        deadKeys.forEach(sseEmitters::remove);
    }
}
