package com.kinghy.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinghy.rag.common.KnowledgeBaseType;
import com.kinghy.rag.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库数据访问层接口
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {
    
    /**
     * 根据用户ID和知识库类型查询知识库列表
     * 
     * @param userId 用户ID
     * @param type 知识库类型
     * @return 知识库列表
     */
    List<KnowledgeBase> selectByUserIdAndType(@Param("userId") Long userId, @Param("type") KnowledgeBaseType type);
    
    /**
     * 查询用户可访问的知识库列表（个人知识库 + 公共知识库）
     * 
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<KnowledgeBase> selectAccessibleByUserId(@Param("userId") Long userId);
    
    /**
     * 根据关键词搜索用户可访问的知识库
     * 
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @return 知识库列表
     */
    List<KnowledgeBase> searchAccessibleByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * 更新知识库文件数量
     * 
     * @param knowledgeBaseId 知识库ID
     * @param increment 增量（可为负数）
     * @return 影响行数
     */
    int updateFileCount(@Param("knowledgeBaseId") Long knowledgeBaseId, @Param("increment") Integer increment);
    
    /**
     * 修复所有知识库的文件计数
     * 
     * @return 修复的知识库数量
     */
    int fixAllFileCount();
}