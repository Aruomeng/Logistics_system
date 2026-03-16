package com.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.aop.OperLog;
import com.logistics.common.Result;
import com.logistics.entity.Notice;
import com.logistics.entity.NoticeAttachment;
import com.logistics.security.SecurityUtil;
import com.logistics.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ========== 管理员操作 ==========

    @OperLog(module = "公告管理", type = "CREATE", desc = "发布公告")
    @PostMapping("/admin/add")
    public Result<Notice> addNotice(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "0") Integer isTop,
            @RequestPart(required = false) List<MultipartFile> attachments) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsTop(isTop);
        notice.setPublisherId(SecurityUtil.getCurrentUserId());
        notice.setPublisherName(SecurityUtil.getCurrentUser().getUsername());
        return Result.ok(noticeService.addNotice(notice, attachments));
    }

    @GetMapping("/admin/list")
    public Result<List<Notice>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String publisherName,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Integer isTop) {
        Page<Notice> result = noticeService.adminList(page, size, title, publisherName, startTime, endTime, isTop);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @OperLog(module = "公告管理", type = "UPDATE", desc = "编辑公告")
    @PutMapping("/admin/update")
    public Result<Void> updateNotice(
            @RequestParam Long noticeId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(defaultValue = "0") Integer isTop,
            @RequestPart(required = false) List<MultipartFile> attachments) {
        Notice notice = new Notice();
        notice.setNoticeId(noticeId);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setIsTop(isTop);
        noticeService.updateNotice(notice, attachments);
        return Result.ok();
    }

    @OperLog(module = "公告管理", type = "DELETE", desc = "删除公告")
    @DeleteMapping("/admin/delete")
    public Result<Void> deleteNotice(@RequestParam Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return Result.ok();
    }

    @OperLog(module = "公告管理", type = "UPDATE", desc = "公告置顶切换")
    @PutMapping("/admin/top")
    public Result<Void> toggleTop(@RequestParam Long noticeId, @RequestParam Integer isTop) {
        noticeService.toggleTop(noticeId, isTop);
        return Result.ok();
    }

    // ========== 公共接口 ==========

    @GetMapping("/common/list")
    public Result<List<Notice>> commonList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<Notice> result = noticeService.commonList(page, size, title, startTime, endTime);
        return Result.ok(result.getRecords(), result.getTotal());
    }

    @GetMapping("/common/detail")
    public Result<Map<String, Object>> getDetail(@RequestParam Long noticeId) {
        Notice notice = noticeService.getDetail(noticeId);
        List<NoticeAttachment> attachments = noticeService.getAttachments(noticeId);
        Map<String, Object> data = new HashMap<>();
        data.put("notice", notice);
        data.put("attachments", attachments);
        return Result.ok(data);
    }

    // ========== SSE 推送 ==========

    @GetMapping("/common/subscribe")
    public SseEmitter subscribe(@RequestParam Long userId) {
        return noticeService.subscribe(userId);
    }
}
