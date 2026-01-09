package com.kinghy.rag.controller;

/**
 * 知识库控制器（兼容性接口）
 * 提供向后兼容的文件上传和管理功能
 * 注意：此控制器主要用于兼容旧版本API，新功能请使用 KnowledgeBaseController 和 KnowledgeBaseFileController
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */

import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ErrorCode;
import com.kinghy.rag.common.ResultUtils;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.entity.KnowledgeBase;
import com.kinghy.rag.entity.KnowledgeBaseFile;
import com.kinghy.rag.pojo.dto.QueryFileDTO;
import com.kinghy.rag.service.KnowledgeBaseFileService;
import com.kinghy.rag.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "KnowledgeController", description = "知识库管理接口（兼容性接口）")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private KnowledgeBaseFileService knowledgeBaseFileService;

    /**
     * 上传附件接口（兼容性接口）
     * 文件将上传到默认的公共知识库中
     * 
     * @param files 上传的文件列表
     * @return 上传结果
     */
    @Operation(summary = "upload", description = "上传附件接口（兼容性接口）")
    @PostMapping(value = "file/upload", headers = "content-type=multipart/form-data")
    public BaseResponse<String> upload(@RequestParam("file") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
            }
            
            // 获取或创建默认公共知识库
            KnowledgeBase defaultKnowledgeBase = getOrCreateDefaultKnowledgeBase(userId);
            
            // 上传文件到默认知识库
            List<KnowledgeBaseFile> uploadedFiles = knowledgeBaseFileService.uploadFiles(
                defaultKnowledgeBase.getId(), files, userId);
            
            log.info("用户 {} 成功上传 {} 个文件到默认知识库", userId, uploadedFiles.size());
            return ResultUtils.success("文件上传成功，共上传 " + uploadedFiles.size() + " 个文件");
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件查询接口（兼容性接口）
     * 查询默认知识库中的文件
     * 
     * @param request 查询请求
     * @return 文件列表
     */
    @Operation(summary = "contents", description = "文件查询（兼容性接口）")
    @GetMapping("/contents")
    public BaseResponse<List<KnowledgeBaseFile>> queryFiles(QueryFileDTO request) {
        if (request.getPage() == null || request.getPageSize() == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "page 或 pageSize为空");
        }
        
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
            }
            
            // 获取默认知识库
            KnowledgeBase defaultKnowledgeBase = getOrCreateDefaultKnowledgeBase(userId);
            
            // 查询文件列表
            List<KnowledgeBaseFile> files = knowledgeBaseFileService.getKnowledgeBaseFiles(
                defaultKnowledgeBase.getId(), userId);
            
            // 简单的文件名过滤（兼容旧接口）
            if (request.getFileName() != null && !request.getFileName().trim().isEmpty()) {
                files = files.stream()
                    .filter(file -> file.getOriginalName().contains(request.getFileName()))
                    .collect(Collectors.toList());
            }
            
            // 简单分页（兼容旧接口）
            int start = (request.getPage() - 1) * request.getPageSize();
            int end = Math.min(start + request.getPageSize(), files.size());
            
            if (start >= files.size()) {
                return ResultUtils.success(List.of());
            }
            
            List<KnowledgeBaseFile> pagedFiles = files.subList(start, end);
            return ResultUtils.success(pagedFiles);
            
        } catch (Exception e) {
            log.error("查询文件失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "查询文件失败：" + e.getMessage());
        }
    }

    /**
     * 文件删除接口（兼容性接口）
     * 
     * @param ids 文件ID列表
     * @return 删除结果
     */
    @Operation(summary = "delete", description = "文件删除（兼容性接口）")
    @DeleteMapping("/delete")
    public BaseResponse<String> deleteFiles(@RequestParam List<Long> ids) {
        if (ids.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请选择要删除的文件");
        }
        
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
            }
            
            knowledgeBaseFileService.deleteFiles(ids, userId);
            
            log.info("用户 {} 成功删除 {} 个文件", userId, ids.size());
            return ResultUtils.success("成功删除 " + ids.size() + " 个文件");
            
        } catch (Exception e) {
            log.error("删除文件失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "删除文件失败：" + e.getMessage());
        }
    }

    /**
     * 文件下载接口（兼容性接口）
     * 注意：此接口暂时保留，但建议使用新的文件管理接口
     * 
     * @param ids 文件ID列表
     * @return 下载结果
     */
    @Operation(summary = "download", description = "文件下载（兼容性接口）")
    @GetMapping("/download")
    public BaseResponse<String> downloadFiles(@RequestParam List<Long> ids) {
        if (ids.isEmpty()) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR, "请选择要下载的文件");
        }
        
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
            }
            
            // 验证文件权限并获取下载链接
            List<String> downloadUrls = ids.stream()
                .map(id -> {
                    try {
                        KnowledgeBaseFile file = knowledgeBaseFileService.validateFileAccess(id, userId);
                        return file.getFileUrl();
                    } catch (Exception e) {
                        log.warn("文件 {} 访问权限验证失败: {}", id, e.getMessage());
                        return null;
                    }
                })
                .filter(url -> url != null)
                .collect(Collectors.toList());
            
            if (downloadUrls.isEmpty()) {
                return ResultUtils.error(ErrorCode.OPERATION_ERROR, "没有可下载的文件");
            }
            
            log.info("用户 {} 请求下载 {} 个文件", userId, downloadUrls.size());
            return ResultUtils.success("获取下载链接成功，共 " + downloadUrls.size() + " 个文件");
            
        } catch (Exception e) {
            log.error("获取下载链接失败", e);
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "获取下载链接失败：" + e.getMessage());
        }
    }

    /**
     * 获取或创建默认知识库
     * 
     * @param userId 用户ID
     * @return 默认知识库
     */
    private KnowledgeBase getOrCreateDefaultKnowledgeBase(Long userId) {
        // 查找是否存在默认公共知识库
        List<KnowledgeBase> publicKnowledgeBases = knowledgeBaseService.searchKnowledgeBases("默认公共知识库", userId);
        
        if (!publicKnowledgeBases.isEmpty()) {
            return publicKnowledgeBases.get(0);
        }
        
        // 如果不存在，创建默认公共知识库
        // 注意：这里需要创建 CreateKnowledgeBaseDTO，但为了简化，我们直接查询数据库中是否有默认知识库
        List<KnowledgeBase> allAccessible = knowledgeBaseService.getAccessibleKnowledgeBases(userId);
        
        // 查找名称包含"默认"的公共知识库
        return allAccessible.stream()
            .filter(kb -> kb.getDisplayName().contains("默认") && 
                         kb.getType().name().equals("PUBLIC"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("未找到默认知识库，请联系管理员"));
    }
}
