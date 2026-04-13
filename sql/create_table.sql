CREATE DATABASE IF NOT EXISTS ai_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
 
USE ai_agent;
 
CREATE TABLE IF NOT EXISTS user
(
    id           BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    account_name VARCHAR(256)                           NOT NULL COMMENT '账号',
    user_password VARCHAR(512)                           NOT NULL COMMENT '密码',
    user_name    VARCHAR(256)                           NULL COMMENT '用户昵称',
    user_avatar  VARCHAR(1024)                          NULL COMMENT '用户头像',
    user_profile VARCHAR(512)                           NULL COMMENT '用户简介',
    user_role    VARCHAR(256) DEFAULT 'user'            NOT NULL COMMENT '用户角色：user/admin',
    create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete    TINYINT      DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_account_name (account_name)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;
 
CREATE TABLE IF NOT EXISTS chat_message
(
    id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    conversation_id VARCHAR(256)                        NOT NULL COMMENT '会话ID',
    message_type    VARCHAR(50)                         NOT NULL COMMENT '消息类型：user/assistant/system',
    content         TEXT                                NOT NULL COMMENT '消息内容',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete       TINYINT  DEFAULT 0                 NOT NULL COMMENT '是否删除',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_create_time (create_time)
) COMMENT '聊天记录' COLLATE = utf8mb4_unicode_ci;
 
CREATE TABLE IF NOT EXISTS conversation
(
    id              BIGINT AUTO_INCREMENT COMMENT 'id' PRIMARY KEY,
    conversation_id VARCHAR(256)                        NOT NULL COMMENT '会话ID（UUID）',
    user_id         BIGINT                              NOT NULL COMMENT '用户ID',
    ai_type         VARCHAR(100)                        NOT NULL COMMENT 'AI类型',
    title           VARCHAR(500)                        NULL COMMENT '会话标题',
    description     TEXT                                NULL COMMENT '会话描述',
    status          VARCHAR(50)  DEFAULT 'active'       NOT NULL COMMENT '会话状态',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete       TINYINT      DEFAULT 0               NOT NULL COMMENT '是否删除',
    UNIQUE KEY uk_conversation_id (conversation_id),
    INDEX idx_user_id (user_id),
    INDEX idx_ai_type (ai_type),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '会话表' COLLATE = utf8mb4_unicode_ci;

-- 插入管理员用户（密码：12345678）
INSERT INTO user (id, account_name, user_password, user_name, user_role) 
VALUES (1, 'admin', '482c811da5d5b4bc6d497ffa98491e38', '管理员', 'admin')
ON DUPLICATE KEY UPDATE account_name = account_name;

-- 插入测试用户（便于测试）
INSERT INTO user (account_name, user_password, user_name, user_role) 
VALUES 
('testuser1', '482c811da5d5b4bc6d497ffa98491e38', '测试用户1', 'user'),
('testuser2', '482c811da5d5b4bc6d497ffa98491e38', '测试用户2', 'user'),
('testuser3', '482c811da5d5b4bc6d497ffa98491e38', '测试用户3', 'user')
ON DUPLICATE KEY UPDATE account_name = VALUES(account_name);
