-- 添加 note_data 字段到 messages 表，用于存储笔记分享的数据

ALTER TABLE messages ADD COLUMN note_data TEXT COMMENT '笔记数据（JSON格式，用于笔记分享消息）' AFTER type;

-- 更新 type 字段注释
ALTER TABLE messages MODIFY COLUMN type INT DEFAULT 1 COMMENT '消息类型：1=文本，2=图片，3=语音，4=笔记分享';
