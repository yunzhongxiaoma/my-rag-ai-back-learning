-- 修复知识库文件计数脚本
-- 
-- @author yunzhongxiaoma
-- @since 1.0.0

-- 更新所有知识库的文件计数，基于实际的文件数量
UPDATE tb_knowledge_base kb
SET file_count = (
    SELECT COUNT(*)
    FROM tb_knowledge_base_file kbf
    WHERE kbf.knowledge_base_id = kb.id
),
update_time = NOW()
WHERE EXISTS (
    SELECT 1
    FROM tb_knowledge_base_file kbf
    WHERE kbf.knowledge_base_id = kb.id
);

-- 查询验证结果
SELECT 
    kb.id,
    kb.name,
    kb.display_name,
    kb.type,
    kb.file_count as current_file_count,
    (SELECT COUNT(*) FROM tb_knowledge_base_file kbf WHERE kbf.knowledge_base_id = kb.id) as actual_file_count
FROM tb_knowledge_base kb
ORDER BY kb.id;