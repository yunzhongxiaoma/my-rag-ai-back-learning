package com.kinghy.rag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinghy.rag.common.ErrorCode;
import com.kinghy.rag.common.KnowledgeBaseType;
import com.kinghy.rag.entity.KnowledgeBase;
import com.kinghy.rag.exception.BusinessException;
import com.kinghy.rag.mapper.KnowledgeBaseMapper;
import com.kinghy.rag.pojo.dto.CreateKnowledgeBaseDTO;
import com.kinghy.rag.pojo.dto.UpdateKnowledgeBaseDTO;
import com.kinghy.rag.service.KnowledgeBaseService;
import com.kinghy.rag.service.VectorStoreManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 知识库服务实现类
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Slf4j
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> 
        implements KnowledgeBaseService {
    
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    
    @Autowired
    private VectorStoreManager vectorStoreManager;
    
    @Override
    @Transactional
    public KnowledgeBase createKnowledgeBase(CreateKnowledgeBaseDTO dto, Long userId) {
        log.info("创建知识库，用户ID: {}, 知识库名称: {}, 类型: {}", userId, dto.getDisplayName(), dto.getType());
        
        // 验证输入参数
        validateCreateKnowledgeBaseDTO(dto);
        
        // 生成唯一的知识库标识名
        String name = generateUniqueName(dto.getDisplayName());
        
        // 验证名称唯一性
        if (isNameExists(name)) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NAME_EXISTS);
        }
        
        // 构建知识库实体
        KnowledgeBase knowledgeBase = KnowledgeBase.builder()
                .name(name)
                .displayName(dto.getDisplayName())
                .type(dto.getType())
                .creatorId(userId.intValue())
                .description(dto.getDescription())
                .fileCount(0)
                .vectorCollectionName(generateVectorCollectionName(null)) // 先设置临时值
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        // 保存到数据库
        boolean success = save(knowledgeBase);
        if (!success) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "知识库创建失败");
        }
        
        // 更新向量集合名称（使用实际的ID）
        String collectionName = generateVectorCollectionName(knowledgeBase.getId());
        knowledgeBase.setVectorCollectionName(collectionName);
        updateById(knowledgeBase);
        
        // 创建向量集合
        try {
            vectorStoreManager.createCollection(knowledgeBase.getId(), collectionName);
        } catch (Exception e) {
            log.error("创建向量集合失败，回滚知识库创建", e);
            // 回滚：删除已创建的知识库记录
            removeById(knowledgeBase.getId());
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "创建向量集合失败: " + e.getMessage());
        }
        
        log.info("知识库创建成功，ID: {}, 名称: {}, 向量集合: {}", 
                knowledgeBase.getId(), knowledgeBase.getDisplayName(), collectionName);
        return knowledgeBase;
    }
    
    @Override
    public List<KnowledgeBase> getUserKnowledgeBases(Long userId, KnowledgeBaseType type) {
        log.info("查询用户知识库，用户ID: {}, 类型: {}", userId, type);
        
        if (type != null) {
            return knowledgeBaseMapper.selectByUserIdAndType(userId, type);
        } else {
            // 查询用户的所有个人知识库
            return knowledgeBaseMapper.selectByUserIdAndType(userId, KnowledgeBaseType.PERSONAL);
        }
    }
    
    @Override
    public List<KnowledgeBase> getAccessibleKnowledgeBases(Long userId) {
        log.info("查询用户可访问的知识库，用户ID: {}", userId);
        return knowledgeBaseMapper.selectAccessibleByUserId(userId);
    }
    
    @Override
    @Transactional
    public KnowledgeBase updateKnowledgeBase(Long id, UpdateKnowledgeBaseDTO dto, Long userId) {
        log.info("更新知识库，ID: {}, 用户ID: {}", id, userId);
        
        // 验证权限
        KnowledgeBase knowledgeBase = validateAccess(id, userId);
        
        // 验证是否为创建者（只有创建者可以修改）
        if (!knowledgeBase.getCreatorId().equals(userId.intValue())) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED, "只有创建者可以修改知识库");
        }
        
        // 更新字段
        if (StringUtils.hasText(dto.getDisplayName())) {
            knowledgeBase.setDisplayName(dto.getDisplayName());
        }
        if (dto.getDescription() != null) {
            knowledgeBase.setDescription(dto.getDescription());
        }
        knowledgeBase.setUpdateTime(LocalDateTime.now());
        
        // 保存更新
        boolean success = updateById(knowledgeBase);
        if (!success) {
            throw new BusinessException(ErrorCode.UPDATE_ERROR, "知识库更新失败");
        }
        
        log.info("知识库更新成功，ID: {}", id);
        return knowledgeBase;
    }
    
    @Override
    @Transactional
    public void deleteKnowledgeBase(Long id, Long userId) {
        log.info("删除知识库，ID: {}, 用户ID: {}", id, userId);
        
        // 验证权限
        KnowledgeBase knowledgeBase = validateAccess(id, userId);
        
        // 验证是否为创建者（只有创建者可以删除）
        if (!knowledgeBase.getCreatorId().equals(userId.intValue())) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED, "只有创建者可以删除知识库");
        }
        
        // 删除向量集合
        try {
            if (knowledgeBase.getVectorCollectionName() != null) {
                vectorStoreManager.deleteCollection(id, knowledgeBase.getVectorCollectionName());
            }
        } catch (Exception e) {
            log.warn("删除向量集合失败，但继续删除知识库: {}", e.getMessage());
        }
        
        // 删除知识库（级联删除文件由数据库外键约束处理）
        boolean success = removeById(id);
        if (!success) {
            throw new BusinessException(ErrorCode.DELETE_ERROR, "知识库删除失败");
        }
        
        log.info("知识库删除成功，ID: {}", id);
    }
    
    @Override
    public List<KnowledgeBase> searchKnowledgeBases(String keyword, Long userId) {
        log.info("搜索知识库，关键词: {}, 用户ID: {}", keyword, userId);
        
        if (!StringUtils.hasText(keyword)) {
            return getAccessibleKnowledgeBases(userId);
        }
        
        return knowledgeBaseMapper.searchAccessibleByKeyword(userId, keyword);
    }
    
    @Override
    public KnowledgeBase validateAccess(Long knowledgeBaseId, Long userId) {
        // 查询知识库
        KnowledgeBase knowledgeBase = getById(knowledgeBaseId);
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_NOT_FOUND);
        }
        
        // 验证访问权限
        boolean hasAccess = false;
        
        if (knowledgeBase.getType() == KnowledgeBaseType.PUBLIC) {
            // 公共知识库所有人都可以访问
            hasAccess = true;
        } else if (knowledgeBase.getType() == KnowledgeBaseType.PERSONAL) {
            // 个人知识库只有创建者可以访问
            hasAccess = knowledgeBase.getCreatorId().equals(userId.intValue());
        }
        
        if (!hasAccess) {
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ACCESS_DENIED);
        }
        
        return knowledgeBase;
    }
    
    @Override
    @Transactional
    public void updateFileCount(Long knowledgeBaseId, Integer increment) {
        log.info("更新知识库文件数量，ID: {}, 增量: {}", knowledgeBaseId, increment);
        
        int affectedRows = knowledgeBaseMapper.updateFileCount(knowledgeBaseId, increment);
        if (affectedRows == 0) {
            log.warn("更新知识库文件数量失败，知识库可能不存在，ID: {}", knowledgeBaseId);
        }
    }
    
    @Override
    public String generateVectorCollectionName(Long knowledgeBaseId) {
        if (knowledgeBaseId != null) {
            return "kb_" + knowledgeBaseId;
        } else {
            // 临时名称，后续会更新
            return "kb_temp_" + System.currentTimeMillis();
        }
    }
    
    @Override
    @Transactional
    public int fixFileCount() {
        log.info("开始修复所有知识库的文件计数");
        
        int fixedCount = knowledgeBaseMapper.fixAllFileCount();
        
        log.info("文件计数修复完成，共修复了 {} 个知识库", fixedCount);
        return fixedCount;
    }
    
    /**
     * 生成唯一的知识库标识名
     * 
     * @param displayName 显示名称
     * @return 唯一标识名
     */
    private String generateUniqueName(String displayName) {
        // 基于显示名称生成标识名，去除特殊字符并转换为小写
        String baseName = displayName.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "")
                .toLowerCase();
        
        // 如果基础名称为空，使用默认前缀
        if (baseName.isEmpty()) {
            baseName = "kb";
        }
        
        // 添加时间戳和随机数确保唯一性
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        
        return baseName + "_" + timestamp + "_" + uuid;
    }
    
    /**
     * 检查知识库名称是否已存在
     * 
     * @param name 知识库标识名
     * @return 是否存在
     */
    private boolean isNameExists(String name) {
        LambdaQueryWrapper<KnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KnowledgeBase::getName, name);
        return count(queryWrapper) > 0;
    }
    
    /**
     * 验证创建知识库DTO
     * 
     * @param dto 创建知识库DTO
     */
    private void validateCreateKnowledgeBaseDTO(CreateKnowledgeBaseDTO dto) {
        if (dto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        
        if (!StringUtils.hasText(dto.getDisplayName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库名称不能为空");
        }
        
        if (dto.getDisplayName().length() > 200) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库名称长度不能超过200个字符");
        }
        
        if (dto.getType() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库类型不能为空");
        }
        
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库描述长度不能超过1000个字符");
        }
    }
}