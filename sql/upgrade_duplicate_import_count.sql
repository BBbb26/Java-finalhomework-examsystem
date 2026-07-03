USE exam_system;

-- 已执行过旧升级脚本的数据库，只需单独增加此字段。
ALTER TABLE question
    ADD COLUMN duplicate_import_count INT NOT NULL DEFAULT 0;

