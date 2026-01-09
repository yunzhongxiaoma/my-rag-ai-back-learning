-- ----------------------------
-- Table structure for vector_store
-- ----------------------------
DROP TABLE IF EXISTS `vector_store`;
CREATE TABLE `vector_store` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `content` TEXT,
                                `metadata` JSON,
                                `embedding` JSON COMMENT '向量数据，存储为JSON数组，原PostgreSQL为vector(1536)',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------
-- Table structure for tb_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
                           `id` INT NOT NULL AUTO_INCREMENT,
                           `name` VARCHAR(255) NOT NULL COMMENT '姓名',
                           `user_name` VARCHAR(255) NOT NULL COMMENT '用户名',
                           `password` VARCHAR(255) NOT NULL COMMENT '密码',
                           `phone` VARCHAR(255) NOT NULL COMMENT '手机号',
                           `sex` VARCHAR(255) NOT NULL COMMENT '性别',
                           `id_number` VARCHAR(255) NOT NULL COMMENT '身份证号',
                           `status` INT NOT NULL DEFAULT 1 COMMENT '状态 0：禁用 1：启用',
                           `create_time` DATE COMMENT '创建时间',
                           `update_time` DATE COMMENT '更新时间',
                           `create_user` BIGINT COMMENT '创建人',
                           `update_user` BIGINT COMMENT '修改人',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=666498 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ----------------------------
-- Records of tb_user
-- ----------------------------
INSERT INTO `tb_user` (`id`, `name`, `user_name`, `password`, `phone`, `sex`, `id_number`, `status`, `create_time`, `update_time`, `create_user`, `update_user`)
VALUES (666497, '管理员', 'admin', '21232f297a57a5a743894a0e4a801fc3', '13800138000', '男', '11010519491231002X', 1, '2025-03-03', '2025-03-03', NULL, NULL);


-- ----------------------------
-- Table structure for tb_knowledge_base
-- ----------------------------
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

-- ----------------------------
-- Table structure for tb_knowledge_base_file
-- ----------------------------
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

-- ----------------------------
-- Records of tb_knowledge_base
-- ----------------------------
INSERT INTO `tb_knowledge_base` (`name`, `display_name`, `type`, `creator_id`, `description`) 
VALUES ('default-public', '默认公共知识库', 'PUBLIC', 666497, '系统默认的公共知识库');


-- ----------------------------
-- Table structure for log_info
-- ----------------------------
DROP TABLE IF EXISTS `log_info`;
CREATE TABLE `log_info` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT,
                            `method_name` VARCHAR(255) COMMENT '方法名',
                            `class_name` VARCHAR(255) COMMENT '类目',
                            `request_time` DATE COMMENT '请求时间戳',
                            `request_params` TEXT COMMENT '请求参数',
                            `response` TEXT COMMENT '响应结果',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日志信息表';


-- ----------------------------
-- Table structure for sensitive_word
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_word`;
CREATE TABLE `sensitive_word` (
                                  `id` INT NOT NULL AUTO_INCREMENT,
                                  `word` VARCHAR(255) COMMENT '敏感词内容',
                                  `category` VARCHAR(255) COMMENT '敏感词类别',
                                  `status` VARCHAR(50) COMMENT '敏感词状态',
                                  `created_at` VARCHAR(50) COMMENT '创建时间戳',
                                  `updated_at` VARCHAR(50) COMMENT '更新时间戳',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';


-- ----------------------------
-- Table structure for word_frequency
-- ----------------------------
DROP TABLE IF EXISTS `word_frequency`;
CREATE TABLE `word_frequency` (
                                  `id` INT NOT NULL AUTO_INCREMENT,
                                  `word` VARCHAR(255) COMMENT '分词',
                                  `count_num` INT COMMENT '出现频次',
                                  `business_type` VARCHAR(255) COMMENT '业务类型',
                                  `create_time` DATE COMMENT '创建时间',
                                  `update_time` DATE COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='词频统计表';


-- ----------------------------
-- Table structure for sensitive_category
-- ----------------------------
DROP TABLE IF EXISTS `sensitive_category`;
CREATE TABLE `sensitive_category` (
                                      `id` INT NOT NULL AUTO_INCREMENT,
                                      `category_name` VARCHAR(255) COMMENT '分类名',
                                      `created_time` DATE COMMENT '创建时间',
                                      `update_time` DATE COMMENT '更新时间',
                                      `status` VARCHAR(50) COMMENT '状态',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词分类表';


-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话唯一标识符',
    `user_id` INT NOT NULL COMMENT '用户ID，关联tb_user表',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '会话标题（基于首条消息生成）',
    `status` TINYINT DEFAULT 1 COMMENT '会话状态：1-活跃，0-已结束',
    `message_count` INT DEFAULT 0 COMMENT '消息数量',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_last_message_time` (`last_message_time`),
    KEY `idx_chat_session_user_last_message` (`user_id`, `last_message_time` DESC),
    FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` VARCHAR(64) NOT NULL COMMENT '会话ID',
    `user_id` INT NOT NULL COMMENT '用户ID',
    `message_type` ENUM('USER', 'ASSISTANT') NOT NULL COMMENT '消息类型：USER-用户消息，ASSISTANT-AI回复',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `metadata` JSON DEFAULT NULL COMMENT '消息元数据（如引用文档、处理时间等）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_chat_message_session_time` (`session_id`, `create_time` DESC),
    KEY `idx_chat_message_user_time` (`user_id`, `create_time` DESC),
    FOREIGN KEY (`session_id`) REFERENCES `chat_session` (`session_id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';