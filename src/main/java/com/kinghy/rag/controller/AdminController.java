package com.kinghy.rag.controller;

import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ResultUtils;
import com.kinghy.rag.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理控制器
 * 提供系统维护和数据修复功能
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    /**
     * 修复知识库文件计数
     * 同步知识库表中的file_count字段与实际文件数量
     * 
     * @return 修复结果
     */
    @PostMapping("/fix-knowledge-base-file-count")
    public BaseResponse<String> fixKnowledgeBaseFileCount() {
        log.info("开始修复知识库文件计数");
        
        try {
            int fixedCount = knowledgeBaseService.fixFileCount();
            String message = String.format("修复完成，共修复了 %d 个知识库的文件计数", fixedCount);
            log.info(message);
            return ResultUtils.success(message);
        } catch (Exception e) {
            log.error("修复知识库文件计数失败", e);
            return ResultUtils.error("修复失败: " + e.getMessage());
        }
    }
}