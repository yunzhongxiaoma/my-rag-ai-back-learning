package com.kinghy.rag.service;

import com.kinghy.rag.entity.AliOssFile;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * OSS文件服务接口
 * 提供基础的OSS文件存储操作，不包含业务逻辑
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
public interface AliOssFileService extends IService<AliOssFile> {

    /**
     * 上传文件到OSS并保存记录
     * 
     * @param file 上传的文件
     * @return OSS文件记录
     */
    AliOssFile uploadFile(MultipartFile file);
    
    /**
     * 批量上传文件到OSS
     * 
     * @param files 文件列表
     * @return OSS文件记录列表
     */
    List<AliOssFile> uploadFiles(List<MultipartFile> files);
    
    /**
     * 删除OSS文件（包括OSS存储和数据库记录）
     * 
     * @param fileId 文件ID
     */
    void deleteOssFile(Long fileId);
    
    /**
     * 批量删除OSS文件
     * 
     * @param fileIds 文件ID列表
     */
    void deleteOssFiles(List<Long> fileIds);
    
    /**
     * 根据URL删除OSS文件
     * 
     * @param fileUrl 文件URL
     */
    void deleteOssFileByUrl(String fileUrl);
    
    /**
     * 获取文件下载URL
     * 
     * @param fileId 文件ID
     * @return 下载URL
     */
    String getDownloadUrl(Long fileId);
}
