package com.kinghy.rag.service;

import com.kinghy.rag.common.ErrorCode;
import com.kinghy.rag.common.KnowledgeBaseType;
import com.kinghy.rag.entity.KnowledgeBase;
import com.kinghy.rag.exception.BusinessException;
import com.kinghy.rag.mapper.KnowledgeBaseMapper;
import com.kinghy.rag.pojo.dto.CreateKnowledgeBaseDTO;
import com.kinghy.rag.service.impl.KnowledgeBaseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * KnowledgeBaseService 测试类
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
public class KnowledgeBaseServiceTest {

    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @InjectMocks
    private KnowledgeBaseServiceImpl knowledgeBaseService;

    private KnowledgeBase testPersonalKnowledgeBase;
    private KnowledgeBase testPublicKnowledgeBase;
    private Long testUserId = 1L;
    private Long otherUserId = 2L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testPersonalKnowledgeBase = KnowledgeBase.builder()
                .id(1L)
                .name("personal_kb_123")
                .displayName("我的个人知识库")
                .type(KnowledgeBaseType.PERSONAL)
                .creatorId(testUserId.intValue())
                .description("这是一个个人知识库")
                .fileCount(0)
                .vectorCollectionName("kb_1")
                .createTime(now)
                .updateTime(now)
                .build();
        
        testPublicKnowledgeBase = KnowledgeBase.builder()
                .id(2L)
                .name("public_kb_456")
                .displayName("公共知识库")
                .type(KnowledgeBaseType.PUBLIC)
                .creatorId(testUserId.intValue())
                .description("这是一个公共知识库")
                .fileCount(5)
                .vectorCollectionName("kb_2")
                .createTime(now)
                .updateTime(now)
                .build();
    }

    @Test
    void testCreateKnowledgeBase_EmptyDisplayName() {
        // 准备测试数据
        CreateKnowledgeBaseDTO dto = CreateKnowledgeBaseDTO.builder()
                .displayName("")
                .type(KnowledgeBaseType.PERSONAL)
                .build();

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            knowledgeBaseService.createKnowledgeBase(dto, testUserId);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("知识库名称不能为空"));
    }

    @Test
    void testCreateKnowledgeBase_NullType() {
        // 准备测试数据
        CreateKnowledgeBaseDTO dto = CreateKnowledgeBaseDTO.builder()
                .displayName("测试知识库")
                .type(null)
                .build();

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            knowledgeBaseService.createKnowledgeBase(dto, testUserId);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("知识库类型不能为空"));
    }

    @Test
    void testCreateKnowledgeBase_DisplayNameTooLong() {
        // 准备测试数据 - 超过200个字符的名称
        String longName = "a".repeat(201);
        CreateKnowledgeBaseDTO dto = CreateKnowledgeBaseDTO.builder()
                .displayName(longName)
                .type(KnowledgeBaseType.PERSONAL)
                .build();

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            knowledgeBaseService.createKnowledgeBase(dto, testUserId);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("知识库名称长度不能超过200个字符"));
    }

    @Test
    void testCreateKnowledgeBase_DescriptionTooLong() {
        // 准备测试数据 - 超过1000个字符的描述
        String longDescription = "a".repeat(1001);
        CreateKnowledgeBaseDTO dto = CreateKnowledgeBaseDTO.builder()
                .displayName("测试知识库")
                .type(KnowledgeBaseType.PERSONAL)
                .description(longDescription)
                .build();

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            knowledgeBaseService.createKnowledgeBase(dto, testUserId);
        });

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("知识库描述长度不能超过1000个字符"));
    }

    @Test
    void testGetAccessibleKnowledgeBases() {
        // 准备测试数据
        List<KnowledgeBase> expectedList = Arrays.asList(testPersonalKnowledgeBase, testPublicKnowledgeBase);

        // Mock 行为
        when(knowledgeBaseMapper.selectAccessibleByUserId(testUserId)).thenReturn(expectedList);

        // 执行测试
        List<KnowledgeBase> result = knowledgeBaseService.getAccessibleKnowledgeBases(testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedList, result);
        
        // 验证方法调用
        verify(knowledgeBaseMapper).selectAccessibleByUserId(testUserId);
    }

    @Test
    void testGetUserKnowledgeBases() {
        // 准备测试数据
        List<KnowledgeBase> expectedList = Arrays.asList(testPersonalKnowledgeBase);

        // Mock 行为
        when(knowledgeBaseMapper.selectByUserIdAndType(testUserId, KnowledgeBaseType.PERSONAL)).thenReturn(expectedList);

        // 执行测试
        List<KnowledgeBase> result = knowledgeBaseService.getUserKnowledgeBases(testUserId, KnowledgeBaseType.PERSONAL);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList, result);
        
        // 验证方法调用
        verify(knowledgeBaseMapper).selectByUserIdAndType(testUserId, KnowledgeBaseType.PERSONAL);
    }

    @Test
    void testGenerateVectorCollectionName() {
        // 测试有ID的情况
        String result1 = knowledgeBaseService.generateVectorCollectionName(123L);
        assertEquals("kb_123", result1);

        // 测试无ID的情况
        String result2 = knowledgeBaseService.generateVectorCollectionName(null);
        assertNotNull(result2);
        assertTrue(result2.startsWith("kb_temp_"));
    }

    @Test
    void testSearchKnowledgeBases() {
        // 准备测试数据
        String keyword = "测试";
        List<KnowledgeBase> expectedList = Arrays.asList(testPersonalKnowledgeBase);

        // Mock 行为
        when(knowledgeBaseMapper.searchAccessibleByKeyword(testUserId, keyword)).thenReturn(expectedList);

        // 执行测试
        List<KnowledgeBase> result = knowledgeBaseService.searchKnowledgeBases(keyword, testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedList, result);
        
        // 验证方法调用
        verify(knowledgeBaseMapper).searchAccessibleByKeyword(testUserId, keyword);
    }

    @Test
    void testSearchKnowledgeBases_EmptyKeyword() {
        // 准备测试数据
        List<KnowledgeBase> expectedList = Arrays.asList(testPersonalKnowledgeBase, testPublicKnowledgeBase);

        // Mock 行为
        when(knowledgeBaseMapper.selectAccessibleByUserId(testUserId)).thenReturn(expectedList);

        // 执行测试
        List<KnowledgeBase> result = knowledgeBaseService.searchKnowledgeBases("", testUserId);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedList, result);
        
        // 验证方法调用
        verify(knowledgeBaseMapper).selectAccessibleByUserId(testUserId);
        verify(knowledgeBaseMapper, never()).searchAccessibleByKeyword(any(), any());
    }

    @Test
    void testUpdateFileCount() {
        // Mock 行为
        when(knowledgeBaseMapper.updateFileCount(1L, 1)).thenReturn(1);

        // 执行测试
        knowledgeBaseService.updateFileCount(1L, 1);

        // 验证方法调用
        verify(knowledgeBaseMapper).updateFileCount(1L, 1);
    }
}