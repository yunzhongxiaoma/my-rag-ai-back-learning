package com.kinghy.rag.controller;

import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ResultUtils;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.entity.KnowledgeBaseFile;
import com.kinghy.rag.service.KnowledgeBaseFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文件管理控制器
 * 提供知识库文件的上传、删除、查询等功能，支持批量操作
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Tag(name = "KnowledgeBaseFileController", description = "知识库文件管理接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/knowledge-base")
public class KnowledgeBaseFileController {

    @Autowired
    private KnowledgeBaseFileService knowledgeBaseFileService;

    /**
     * 上传单个文件到知识库
     * 
     * @param knowledgeBaseId 知识库ID
     * @param file 上传的文件
     * @return 上传后的文件信息
     */
    @Operation(summary = "上传文件到知识库", description = "上传单个文件到指定的知识库")
    @PostMapping("/{knowledgeBaseId}/files")
    public BaseResponse<KnowledgeBaseFile> uploadFile(
            @Parameter(description = "知识库ID") @PathVariable Long knowledgeBaseId,
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        log.info("上传文件到知识库请求, 知识库ID: {}, 文件名: {}", knowledgeBaseId, file.getOriginalFilename());
        
        Long userId = BaseContext.getCurrentId();
        KnowledgeBaseFile uploadedFile = knowledgeBaseFileService.uploadFile(knowledgeBaseId, file, userId);
        
        log.info("文件上传成功, 文件ID: {}", uploadedFile.getId());
        return ResultUtils.success(uploadedFile);
    }

    /**
     * 批量上传文件到知识库
     * 
     * @param knowledgeBaseId 知识库ID
     * @param files 上传的文件列表
     * @return 上传后的文件信息列表
     */
    @Operation(summary = "批量上传文件到知识库", description = "批量上传多个文件到指定的知识库")
    @PostMapping("/{knowledgeBaseId}/files/batch")
    public BaseResponse<List<KnowledgeBaseFile>> uploadFiles(
            @Parameter(description = "知识库ID") @PathVariable Long knowledgeBaseId,
            @Parameter(description = "上传的文件列表") @RequestParam("files") List<MultipartFile> files) {
        log.info("批量上传文件到知识库请求, 知识库ID: {}, 文件数量: {}", knowledgeBaseId, files.size());
        
        Long userId = BaseContext.getCurrentId();
        List<KnowledgeBaseFile> uploadedFiles = knowledgeBaseFileService.uploadFiles(knowledgeBaseId, files, userId);
        
        log.info("批量文件上传成功, 上传文件数量: {}", uploadedFiles.size());
        return ResultUtils.success(uploadedFiles);
    }

    /**
     * 获取知识库文件列表
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 文件列表
     */
    @Operation(summary = "获取知识库文件列表", description = "获取指定知识库下的所有文件")
    @GetMapping("/{knowledgeBaseId}/files")
    public BaseResponse<List<KnowledgeBaseFile>> getKnowledgeBaseFiles(
            @Parameter(description = "知识库ID") @PathVariable Long knowledgeBaseId) {
        log.info("获取知识库文件列表请求, 知识库ID: {}", knowledgeBaseId);
        
        Long userId = BaseContext.getCurrentId();
        List<KnowledgeBaseFile> files = knowledgeBaseFileService.getKnowledgeBaseFiles(knowledgeBaseId, userId);
        
        log.info("获取到 {} 个文件", files.size());
        return ResultUtils.success(files);
    }

    /**
     * 根据文件ID获取文件详情
     * 
     * @param fileId 文件ID
     * @return 文件详情
     */
    @Operation(summary = "获取文件详情", description = "根据文件ID获取文件详细信息")
    @GetMapping("/files/{fileId}")
    public BaseResponse<KnowledgeBaseFile> getFileById(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        log.info("获取文件详情请求, 文件ID: {}", fileId);
        
        Long userId = BaseContext.getCurrentId();
        KnowledgeBaseFile file = knowledgeBaseFileService.validateFileAccess(fileId, userId);
        
        log.info("获取文件详情成功: {}", file.getOriginalName());
        return ResultUtils.success(file);
    }

    /**
     * 删除单个文件
     * 
     * @param fileId 文件ID
     * @return 删除结果
     */
    @Operation(summary = "删除文件", description = "删除指定的知识库文件及其向量数据")
    @DeleteMapping("/files/{fileId}")
    public BaseResponse<Void> deleteFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId) {
        log.info("删除文件请求, 文件ID: {}", fileId);
        
        Long userId = BaseContext.getCurrentId();
        knowledgeBaseFileService.deleteFile(fileId, userId);
        
        log.info("文件删除成功: {}", fileId);
        return ResultUtils.success(null);
    }

    /**
     * 批量删除文件
     * 
     * @param fileIds 文件ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除文件", description = "批量删除多个知识库文件及其向量数据")
    @DeleteMapping("/files/batch")
    public BaseResponse<Void> deleteFiles(
            @Parameter(description = "文件ID列表") @RequestParam List<Long> fileIds) {
        log.info("批量删除文件请求, 文件ID列表: {}", fileIds);
        
        Long userId = BaseContext.getCurrentId();
        knowledgeBaseFileService.deleteFiles(fileIds, userId);
        
        log.info("批量文件删除成功, 删除文件数量: {}", fileIds.size());
        return ResultUtils.success(null);
    }

    /**
     * 删除知识库下的所有文件
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 删除结果
     */
    @Operation(summary = "删除知识库所有文件", description = "删除指定知识库下的所有文件及其向量数据")
    @DeleteMapping("/{knowledgeBaseId}/files/all")
    public BaseResponse<Void> deleteAllFiles(
            @Parameter(description = "知识库ID") @PathVariable Long knowledgeBaseId) {
        log.info("删除知识库所有文件请求, 知识库ID: {}", knowledgeBaseId);
        
        Long userId = BaseContext.getCurrentId();
        knowledgeBaseFileService.deleteAllFilesByKnowledgeBaseId(knowledgeBaseId, userId);
        
        log.info("知识库所有文件删除成功: {}", knowledgeBaseId);
        return ResultUtils.success(null);
    }

    /**
     * 统计知识库文件数量
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 文件数量
     */
    @Operation(summary = "统计知识库文件数量", description = "统计指定知识库下的文件数量")
    @GetMapping("/{knowledgeBaseId}/files/count")
    public BaseResponse<Integer> countFiles(
            @Parameter(description = "知识库ID") @PathVariable Long knowledgeBaseId) {
        log.info("统计知识库文件数量请求, 知识库ID: {}", knowledgeBaseId);
        
        // 验证用户权限（通过获取知识库文件列表来验证）
        Long userId = BaseContext.getCurrentId();
        knowledgeBaseFileService.getKnowledgeBaseFiles(knowledgeBaseId, userId);
        
        int count = knowledgeBaseFileService.countFilesByKnowledgeBaseId(knowledgeBaseId);
        
        log.info("知识库文件数量: {}", count);
        return ResultUtils.success(count);
    }
}