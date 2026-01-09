-- 知识库管理系统数据库重构脚本
-- @author yunzhongxiaoma
-- @since 1.0.0

-- 删除原有的 ali_oss_file 表
DROP TABLE IF EXISTS `ali_oss_file`;

-- 创建知识库表
DROP TABLE IF EXISTS `tb_knowledge_base`;
CREATE TABLE `tb_knowledge_base` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL UNIQUE COMMENT '知识库唯一标识名',
    `display_name` VARCHAR(200) NOT NULL COMMENT '知识库显示名称',
    `type` ENUM('PERSONAL', 'PUBLIC') NOT NULL COMMENT '知识库类型',
    `creator_id` INT NOT NULL COMMENT '创建者用户ID',
    `description` TEXT COMMENT '知识库描述',
    `file_count` INT NOT NULL DEFAULT 0 COMMENT '文件数量',
    `vector_collection_name` VARCHAR(100) COMMENT 'Milvus集合名称',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_name` (`name`),
    FOREIGN KEY (`creator_id`) REFERENCES `tb_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- 创建知识库文件表
DROP TABLE IF EXISTS `tb_knowledge_base_file`;
CREATE TABLE `tb_knowledge_base_file` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '存储文件名',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_url` VARCHAR(500) NOT NULL COMMENT '文件URL',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `vector_ids` TEXT COMMENT '向量ID列表(JSON格式)',
    `upload_user_id` INT NOT NULL COMMENT '上传用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_knowledge_base_id` (`knowledge_base_id`),
    INDEX `idx_upload_user_id` (`upload_user_id`),
    FOREIGN KEY (`knowledge_base_id`) REFERENCES `tb_knowledge_base`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`upload_user_id`) REFERENCES `tb_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文件表';

-- 插入默认的公共知识库
INSERT INTO `tb_knowledge_base` (`name`, `display_name`, `type`, `creator_id`, `description`) 
VALUES ('default-public', '默认公共知识库', 'PUBLIC', 666497, '系统默认的公共知识库');