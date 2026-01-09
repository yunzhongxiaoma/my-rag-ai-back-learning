package com.kinghy.rag.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS文件实体类
 * 用于存储基础的OSS文件信息
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@TableName(value ="ali_oss_file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliOssFile {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 该文件分割出的多段向量文本ID（已废弃，由知识库文件管理）
     * @deprecated 此字段已废弃，向量ID现在由 KnowledgeBaseFile 管理
     */
    @Deprecated
    private String vectorId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}