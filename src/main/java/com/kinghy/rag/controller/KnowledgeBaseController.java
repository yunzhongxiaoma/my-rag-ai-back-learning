package com.kinghy.rag.controller;

import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.KnowledgeBaseType;
import com.kinghy.rag.common.ResultUtils;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.entity.KnowledgeBase;
import com.kinghy.rag.pojo.dto.CreateKnowledgeBaseDTO;
import com.kinghy.rag.pojo.dto.KnowledgeBaseQueryDTO;
import com.kinghy.rag.pojo.dto.UpdateKnowledgeBaseDTO;
import com.kinghy.rag.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库管理控制器
 * 提供知识库的CRUD操作、搜索和权限过滤功能
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Tag(name = "KnowledgeBaseController", description = "知识库管理接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/knowledge-base")
public class KnowledgeBaseController {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 创建知识库
     * 
     * @param dto 创建知识库请求DTO
     * @return 创建的知识库信息
     */
    @Operation(summary = "创建知识库", description = "创建新的知识库，支持个人知识库和公共知识库")
    @PostMapping
    public BaseResponse<KnowledgeBase> createKnowledgeBase(@RequestBody CreateKnowledgeBaseDTO dto) {
        log.info("创建知识库请求: {}", dto);
        
        Long userId = BaseContext.getCurrentId();
        KnowledgeBase knowledgeBase = knowledgeBaseService.createKnowledgeBase(dto, userId);
        
        log.info("知识库创建成功: {}", knowledgeBase.getId());
        return ResultUtils.success(knowledgeBase);
    }

    /**
     * 获取知识库列表
     * 
     * @param type 知识库类型（可选）
     * @return 知识库列表
     */
    @Operation(summary = "获取知识库列表", description = "根据类型获取用户的知识库列表，不指定类型时返回所有类型")
    @GetMapping
    public BaseResponse<List<KnowledgeBase>> getKnowledgeBases(
            @Parameter(description = "知识库类型") @RequestParam(required = false) KnowledgeBaseType type) {
        log.info("获取知识库列表请求, 类型: {}", type);
        
        Long userId = BaseContext.getCurrentId();
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.getUserKnowledgeBases(userId, type);
        
        log.info("获取到 {} 个知识库", knowledgeBases.size());
        return ResultUtils.success(knowledgeBases);
    }

    /**
     * 获取用户可访问的所有知识库
     * 
     * @return 知识库列表
     */
    @Operation(summary = "获取可访问的知识库", description = "获取用户可访问的所有知识库（个人知识库 + 公共知识库）")
    @GetMapping("/accessible")
    public BaseResponse<List<KnowledgeBase>> getAccessibleKnowledgeBases() {
        log.info("获取可访问知识库列表请求");
        
        Long userId = BaseContext.getCurrentId();
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.getAccessibleKnowledgeBases(userId);
        
        log.info("获取到 {} 个可访问知识库", knowledgeBases.size());
        return ResultUtils.success(knowledgeBases);
    }

    /**
     * 搜索知识库
     * 
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @Operation(summary = "搜索知识库", description = "根据关键词搜索用户可访问的知识库")
    @GetMapping("/search")
    public BaseResponse<List<KnowledgeBase>> searchKnowledgeBases(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        log.info("搜索知识库请求, 关键词: {}", keyword);
        
        Long userId = BaseContext.getCurrentId();
        List<KnowledgeBase> knowledgeBases = knowledgeBaseService.searchKnowledgeBases(keyword, userId);
        
        log.info("搜索到 {} 个知识库", knowledgeBases.size());
        return ResultUtils.success(knowledgeBases);
    }

    /**
     * 根据ID获取知识库详情
     * 
     * @param id 知识库ID
     * @return 知识库详情
     */
    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库详细信息")
    @GetMapping("/{id}")
    public BaseResponse<KnowledgeBase> getKnowledgeBaseById(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        log.info("获取知识库详情请求, ID: {}", id);
        
        Long userId = BaseContext.getCurrentId();
        KnowledgeBase knowledgeBase = knowledgeBaseService.validateAccess(id, userId);
        
        log.info("获取知识库详情成功: {}", knowledgeBase.getDisplayName());
        return ResultUtils.success(knowledgeBase);
    }

    /**
     * 更新知识库信息
     * 
     * @param id 知识库ID
     * @param dto 更新知识库请求DTO
     * @return 更新后的知识库信息
     */
    @Operation(summary = "更新知识库", description = "更新知识库的显示名称和描述信息")
    @PutMapping("/{id}")
    public BaseResponse<KnowledgeBase> updateKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id,
            @RequestBody UpdateKnowledgeBaseDTO dto) {
        log.info("更新知识库请求, ID: {}, 数据: {}", id, dto);
        
        Long userId = BaseContext.getCurrentId();
        KnowledgeBase knowledgeBase = knowledgeBaseService.updateKnowledgeBase(id, dto, userId);
        
        log.info("知识库更新成功: {}", knowledgeBase.getId());
        return ResultUtils.success(knowledgeBase);
    }

    /**
     * 删除知识库
     * 
     * @param id 知识库ID
     * @return 删除结果
     */
    @Operation(summary = "删除知识库", description = "删除指定的知识库及其所有文件和向量数据")
    @DeleteMapping("/{id}")
    public BaseResponse<Void> deleteKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        log.info("删除知识库请求, ID: {}", id);
        
        Long userId = BaseContext.getCurrentId();
        knowledgeBaseService.deleteKnowledgeBase(id, userId);
        
        log.info("知识库删除成功: {}", id);
        return ResultUtils.success(null);
    }

    /**
     * 复合查询知识库
     * 
     * @param queryDTO 查询条件
     * @return 查询结果
     */
    @Operation(summary = "复合查询知识库", description = "根据多个条件查询知识库")
    @PostMapping("/query")
    public BaseResponse<List<KnowledgeBase>> queryKnowledgeBases(@RequestBody KnowledgeBaseQueryDTO queryDTO) {
        log.info("复合查询知识库请求: {}", queryDTO);
        
        Long userId = BaseContext.getCurrentId();
        List<KnowledgeBase> knowledgeBases;
        
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            // 如果有关键词，进行搜索
            knowledgeBases = knowledgeBaseService.searchKnowledgeBases(queryDTO.getKeyword(), userId);
            
            // 如果指定了类型，进一步过滤
            if (queryDTO.getType() != null) {
                knowledgeBases = knowledgeBases.stream()
                        .filter(kb -> kb.getType() == queryDTO.getType())
                        .toList();
            }
        } else {
            // 没有关键词，按类型查询
            knowledgeBases = knowledgeBaseService.getUserKnowledgeBases(userId, queryDTO.getType());
        }
        
        log.info("复合查询到 {} 个知识库", knowledgeBases.size());
        return ResultUtils.success(knowledgeBases);
    }
}