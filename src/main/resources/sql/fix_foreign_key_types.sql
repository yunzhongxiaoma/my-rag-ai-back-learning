-- 修复外键类型不兼容问题
-- @author yunzhongxiaoma
-- @since 1.0.0

-- 如果表已存在，需要先删除外键约束，修改字段类型，然后重新添加外键约束

-- 1. 修复 tb_knowledge_base 表
-- 检查表是否存在
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables 
                    WHERE table_schema = DATABASE() AND table_name = 'tb_knowledge_base');

-- 如果表存在，修复字段类型
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE tb_knowledge_base 
     DROP FOREIGN KEY IF EXISTS tb_knowledge_base_ibfk_1,
     MODIFY COLUMN creator_id INT NOT NULL COMMENT "创建者用户ID",
     ADD FOREIGN KEY (creator_id) REFERENCES tb_user(id)', 
    'SELECT "tb_knowledge_base table does not exist" as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 修复 tb_knowledge_base_file 表
-- 检查表是否存在
SET @table_exists = (SELECT COUNT(*) FROM information_schema.tables 
                    WHERE table_schema = DATABASE() AND table_name = 'tb_knowledge_base_file');

-- 如果表存在，修复字段类型
SET @sql = IF(@table_exists > 0, 
    'ALTER TABLE tb_knowledge_base_file 
     DROP FOREIGN KEY IF EXISTS tb_knowledge_base_file_ibfk_2,
     MODIFY COLUMN upload_user_id INT NOT NULL COMMENT "上传用户ID",
     ADD FOREIGN KEY (upload_user_id) REFERENCES tb_user(id)', 
    'SELECT "tb_knowledge_base_file table does not exist" as message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 显示修复完成信息
SELECT 'Foreign key type compatibility issues have been fixed!' as message;