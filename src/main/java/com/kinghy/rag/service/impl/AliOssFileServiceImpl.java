package com.kinghy.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinghy.rag.entity.AliOssFile;
import com.kinghy.rag.exception.BusinessException;
import com.kinghy.rag.common.ErrorCode;
import com.kinghy.rag.service.AliOssFileService;
import com.kinghy.rag.mapper.AliOssFileMapper;
import com.kinghy.rag.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * OSS文件服务实现类
 * 提供基础的OSS文件存储操作
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Slf4j
@Service
public class AliOssFileServiceImpl extends ServiceImpl<AliOssFileMapper, AliOssFile>
    implements AliOssFileService {

    @Autowired
    private AliOssFileMapper aliOssFileMapper;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AliOssFile uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
            }
            
            // 生成唯一文件名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = UUID.randomUUID() + extension;
            
            // 上传到OSS
            String url = aliOssUtil.upload(file.getBytes(), objectName);
            
            // 保存文件记录
            long currMillis = System.currentTimeMillis();
            AliOssFile ossFile = AliOssFile.builder()
                    .fileName(originalFilename)
                    .url(url)
                    .createTime(new Date(currMillis))
                    .updateTime(new Date(currMillis))
                    .build();
            
            this.save(ossFile);
            
            log.info("文件上传成功: {} -> {}", originalFilename, url);
            return ossFile;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AliOssFile> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件列表不能为空");
        }
        
        List<AliOssFile> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                AliOssFile ossFile = uploadFile(file);
                uploadedFiles.add(ossFile);
            } catch (Exception e) {
                log.error("批量上传中文件 {} 上传失败", file.getOriginalFilename(), e);
                // 继续上传其他文件，不中断整个批量上传过程
            }
        }
        
        if (uploadedFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "所有文件上传失败");
        }
        
        log.info("批量上传完成，成功上传 {} 个文件，共 {} 个文件", uploadedFiles.size(), files.size());
        return uploadedFiles;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOssFile(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID不能为空");
        }
        
        AliOssFile ossFile = this.getById(fileId);
        if (ossFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        
        try {
            // 删除OSS文件
            aliOssUtil.deleteOss(ossFile.getUrl());
            
            // 删除数据库记录
            this.removeById(fileId);
            
            log.info("OSS文件删除成功: {} (ID: {})", ossFile.getFileName(), fileId);
            
        } catch (Exception e) {
            log.error("删除OSS文件失败: {} (ID: {})", ossFile.getFileName(), fileId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOssFiles(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID列表不能为空");
        }
        
        List<AliOssFile> ossFiles = this.listByIds(fileIds);
        if (ossFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到要删除的文件");
        }
        
        int successCount = 0;
        for (AliOssFile ossFile : ossFiles) {
            try {
                deleteOssFile(ossFile.getId());
                successCount++;
            } catch (Exception e) {
                log.error("批量删除中文件 {} 删除失败", ossFile.getFileName(), e);
                // 继续删除其他文件
            }
        }
        
        log.info("批量删除完成，成功删除 {} 个文件，共 {} 个文件", successCount, ossFiles.size());
    }

    @Override
    public void deleteOssFileByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件URL不能为空");
        }
        
        try {
            aliOssUtil.deleteOss(fileUrl);
            log.info("根据URL删除OSS文件成功: {}", fileUrl);
        } catch (Exception e) {
            log.error("根据URL删除OSS文件失败: {}", fileUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除文件失败: " + e.getMessage());
        }
    }

    @Override
    public String getDownloadUrl(Long fileId) {
        if (fileId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件ID不能为空");
        }
        
        AliOssFile ossFile = this.getById(fileId);
        if (ossFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        
        return ossFile.getUrl();
    }

    /**
     * 从URL中提取文件名
     * 
     * @param url 文件URL
     * @return 文件名
     */
    public static String extractFileName(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "";
        }
        
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return url;
        }
        
        return url.substring(lastSlashIndex + 1);
    }
}




