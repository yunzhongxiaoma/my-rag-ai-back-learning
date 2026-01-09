package com.kinghy.rag.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinghy.rag.common.ErrorCode;
import com.kinghy.rag.entity.KnowledgeBase;
import com.kinghy.rag.entity.KnowledgeBaseFile;
import com.kinghy.rag.exception.BusinessException;
import com.kinghy.rag.mapper.KnowledgeBaseFileMapper;
import com.kinghy.rag.service.KnowledgeBaseFileService;
import com.kinghy.rag.service.KnowledgeBaseService;
import com.kinghy.rag.service.VectorStoreManager;
import com.kinghy.rag.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 知识库文件服务实现类
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Slf4j
@Service
public class KnowledgeBaseFileServiceImpl extends ServiceImpl<KnowledgeBaseFileMapper, KnowledgeBaseFile> 
        implements KnowledgeBaseFileService {
    
    @Autowired
    private KnowledgeBaseFileMapper knowledgeBaseFileMapper;
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    @Autowired
    private AliOssUtil aliOssUtil;
    
    @Autowired
    private VectorStoreManager vectorStoreManager;
    
    @Override
    @Transactional
    public KnowledgeBaseFile uploadFile(Long knowledgeBaseId, MultipartFile file, Long userId) {
        log.info("上传文件到知识库，知识库ID: {}, 文件名: {}, 用户ID: {}", 
                knowledgeBaseId, file.getOriginalFilename(), userId);
        
        // 验证知识库访问权限
        KnowledgeBase knowledgeBase = knowledgeBaseService.validateAccess(knowledgeBaseId, userId);
        
        // 验证文件
        validateFile(file);
        
        try {
            // 生成唯一文件名
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            
            // 上传到OSS
            String fileUrl = aliOssUtil.upload(file.getBytes(), fileName);
            
            // 处理文档向量化
            List<String> vectorIds = processDocumentVectorization(file, knowledgeBaseId);
            
            // 创建文件记录
            KnowledgeBaseFile knowledgeBaseFile = KnowledgeBaseFile.builder()
                    .knowledgeBaseId(knowledgeBaseId)
                    .fileName(fileName)
                    .originalName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileSize(file.getSize())
                    .fileType(getFileType(file.getOriginalFilename()))
                    .vectorIds(JSON.toJSONString(vectorIds))
                    .uploadUserId(userId.intValue())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            
            // 保存文件记录
            boolean success = save(knowledgeBaseFile);
            if (!success) {
                // 如果数据库保存失败，删除已上传的文件
                aliOssUtil.deleteOss(fileUrl);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件记录保存失败");
            }
            
            // 更新知识库文件数量
            knowledgeBaseService.updateFileCount(knowledgeBaseId, 1);
            
            log.info("文件上传成功，文件ID: {}, 文件名: {}, 向量数量: {}", 
                    knowledgeBaseFile.getId(), fileName, vectorIds.size());
            return knowledgeBaseFile;
            
        } catch (IOException e) {
            log.error("文件读取失败", e);
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件读取失败");
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public List<KnowledgeBaseFile> uploadFiles(Long knowledgeBaseId, List<MultipartFile> files, Long userId) {
        log.info("批量上传文件到知识库，知识库ID: {}, 文件数量: {}, 用户ID: {}", 
                knowledgeBaseId, files.size(), userId);
        
        List<KnowledgeBaseFile> uploadedFiles = new ArrayList<>();
        List<String> uploadedUrls = new ArrayList<>();
        
        try {
            for (MultipartFile file : files) {
                KnowledgeBaseFile uploadedFile = uploadFile(knowledgeBaseId, file, userId);
                uploadedFiles.add(uploadedFile);
                uploadedUrls.add(uploadedFile.getFileUrl());
            }
            
            log.info("批量文件上传成功，共上传 {} 个文件", uploadedFiles.size());
            return uploadedFiles;
            
        } catch (Exception e) {
            log.error("批量文件上传失败，开始回滚", e);
            
            // 回滚：删除已上传的文件
            for (String url : uploadedUrls) {
                try {
                    aliOssUtil.deleteOss(url);
                } catch (Exception deleteException) {
                    log.error("回滚删除文件失败: {}", url, deleteException);
                }
            }
            
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        log.info("删除知识库文件，文件ID: {}, 用户ID: {}", fileId, userId);
        
        // 验证文件访问权限
        KnowledgeBaseFile file = validateFileAccess(fileId, userId);
        
        try {
            // 删除向量数据
            if (file.getVectorIds() != null && !file.getVectorIds().isEmpty()) {
                List<String> vectorIds = JSON.parseArray(file.getVectorIds(), String.class);
                vectorStoreManager.deleteVectors(file.getKnowledgeBaseId(), vectorIds);
            }
            
            // 删除OSS文件
            aliOssUtil.deleteOss(file.getFileUrl());
            
            // 删除数据库记录
            boolean success = removeById(fileId);
            if (!success) {
                throw new BusinessException(ErrorCode.DELETE_ERROR, "文件记录删除失败");
            }
            
            // 更新知识库文件数量
            knowledgeBaseService.updateFileCount(file.getKnowledgeBaseId(), -1);
            
            log.info("文件删除成功，文件ID: {}", fileId);
            
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new BusinessException(ErrorCode.DELETE_ERROR, "文件删除失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void deleteFiles(List<Long> fileIds, Long userId) {
        log.info("批量删除知识库文件，文件ID列表: {}, 用户ID: {}", fileIds, userId);
        
        for (Long fileId : fileIds) {
            deleteFile(fileId, userId);
        }
        
        log.info("批量文件删除成功，共删除 {} 个文件", fileIds.size());
    }
    
    @Override
    public List<KnowledgeBaseFile> getKnowledgeBaseFiles(Long knowledgeBaseId, Long userId) {
        log.info("查询知识库文件列表，知识库ID: {}, 用户ID: {}", knowledgeBaseId, userId);
        
        // 验证知识库访问权限
        knowledgeBaseService.validateAccess(knowledgeBaseId, userId);
        
        return knowledgeBaseFileMapper.selectByKnowledgeBaseId(knowledgeBaseId);
    }
    
    @Override
    @Transactional
    public void deleteAllFilesByKnowledgeBaseId(Long knowledgeBaseId, Long userId) {
        log.info("删除知识库所有文件，知识库ID: {}, 用户ID: {}", knowledgeBaseId, userId);
        
        // 验证知识库访问权限
        knowledgeBaseService.validateAccess(knowledgeBaseId, userId);
        
        // 获取所有文件
        List<KnowledgeBaseFile> files = knowledgeBaseFileMapper.selectByKnowledgeBaseId(knowledgeBaseId);
        
        // 删除OSS文件
        for (KnowledgeBaseFile file : files) {
            try {
                // 删除向量数据
                if (file.getVectorIds() != null && !file.getVectorIds().isEmpty()) {
                    List<String> vectorIds = JSON.parseArray(file.getVectorIds(), String.class);
                    vectorStoreManager.deleteVectors(file.getKnowledgeBaseId(), vectorIds);
                }
                
                // 删除OSS文件
                aliOssUtil.deleteOss(file.getFileUrl());
            } catch (Exception e) {
                log.error("删除文件资源失败: {}", file.getFileUrl(), e);
            }
        }
        
        // 删除数据库记录
        int deletedCount = knowledgeBaseFileMapper.deleteByKnowledgeBaseId(knowledgeBaseId);
        
        log.info("知识库文件删除完成，删除数量: {}", deletedCount);
    }
    
    @Override
    public KnowledgeBaseFile validateFileAccess(Long fileId, Long userId) {
        // 查询文件信息
        KnowledgeBaseFile file = getById(fileId);
        if (file == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_FILE_NOT_FOUND);
        }
        
        // 验证知识库访问权限
        knowledgeBaseService.validateAccess(file.getKnowledgeBaseId(), userId);
        
        return file;
    }
    
    @Override
    public int countFilesByKnowledgeBaseId(Long knowledgeBaseId) {
        return knowledgeBaseFileMapper.countByKnowledgeBaseId(knowledgeBaseId);
    }
    
    /**
     * 处理文档向量化
     * 
     * @param file 上传的文件
     * @param knowledgeBaseId 知识库ID
     * @return 向量ID列表
     */
    private List<String> processDocumentVectorization(MultipartFile file, Long knowledgeBaseId) {
        try {
            log.info("开始处理文档向量化，文件: {}, 知识库ID: {}", file.getOriginalFilename(), knowledgeBaseId);
            
            // 使用Tika读取文档内容
            ByteArrayResource resource = new ByteArrayResource(file.getBytes());
            TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
            List<Document> documents = tikaReader.get();
            
            // 使用TokenTextSplitter分割文档
            TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = tokenTextSplitter.apply(documents);
            
            // 为每个文档片段添加知识库元数据
            for (Document document : splitDocuments) {
                document.getMetadata().put("knowledge_base_id", knowledgeBaseId.toString());
                document.getMetadata().put("file_name", file.getOriginalFilename());
                document.getMetadata().put("file_type", getFileType(file.getOriginalFilename()));
            }
            
            // 添加到向量存储
            List<String> vectorIds = vectorStoreManager.addDocuments(knowledgeBaseId, splitDocuments);
            
            log.info("文档向量化完成，生成 {} 个向量", vectorIds.size());
            return vectorIds;
            
        } catch (Exception e) {
            log.error("文档向量化处理失败", e);
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "文档向量化处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证上传文件
     * 
     * @param file 上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件名不能为空");
        }
        
        // 检查文件大小（限制为50MB）
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.FILE_ERROR, "文件大小不能超过50MB");
        }
        
        // 检查文件类型
        String fileType = getFileType(originalFilename);
        if (!isSupportedFileType(fileType)) {
            throw new BusinessException(ErrorCode.FILE_ERROR, 
                    "不支持的文件类型，支持的类型：PDF、DOC、DOCX、TXT、MD");
        }
    }
    
    /**
     * 生成唯一文件名
     * 
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return "kb_files/" + timestamp + "_" + uuid + extension;
    }
    
    /**
     * 获取文件类型
     * 
     * @param filename 文件名
     * @return 文件类型
     */
    private String getFileType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        
        return "";
    }
    
    /**
     * 检查是否为支持的文件类型
     * 
     * @param fileType 文件类型
     * @return 是否支持
     */
    private boolean isSupportedFileType(String fileType) {
        String[] supportedTypes = {"pdf", "doc", "docx", "txt", "md"};
        for (String supportedType : supportedTypes) {
            if (supportedType.equalsIgnoreCase(fileType)) {
                return true;
            }
        }
        return false;
    }
}