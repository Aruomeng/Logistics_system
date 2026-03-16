package com.logistics.service;

import com.logistics.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务 - 本地存储
 */
@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 上传文件，返回相对访问路径
     */
    public String upload(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 按日期分目录
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dir = uploadDir + "/" + subDir + "/" + datePath;
        File dirFile = new File(dir);
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            throw new BusinessException("创建上传目录失败");
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 保存文件
        File dest = new File(dir + "/" + fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }

        // 返回访问路径
        return "/files/" + subDir + "/" + datePath + "/" + fileName;
    }

    /**
     * 获取文件类型（后缀名）
     */
    public String getFileType(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "unknown";
    }
}
