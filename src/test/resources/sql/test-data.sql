-- 插入测试用户
INSERT INTO tb_user (id, username, password, email, status) VALUES 
(1, 'testuser1', '$2a$10$test.password.hash1', 'test1@example.com', 1),
(2, 'testuser2', '$2a$10$test.password.hash2', 'test2@example.com', 1),
(3, 'testuser3', '$2a$10$test.password.hash3', 'test3@example.com', 1);