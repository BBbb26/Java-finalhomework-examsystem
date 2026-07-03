USE exam_system;

-- 题目逻辑删除：历史考试记录仍可继续引用原题。
ALTER TABLE question
    ADD COLUMN is_deleted TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN deleted_at DATETIME NULL,
    ADD COLUMN duplicate_import_count INT NOT NULL DEFAULT 0;

-- 错题去重、累计错误次数和掌握状态。
ALTER TABLE wrong_question
    ADD COLUMN wrong_count INT NOT NULL DEFAULT 1,
    ADD COLUMN mastered TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP;

-- 旧数据中同一学生、同一道题只保留最近一条。
DELETE w1
FROM wrong_question w1
JOIN wrong_question w2
  ON w1.username = w2.username
 AND w1.question_id = w2.question_id
 AND w1.id < w2.id;

CREATE UNIQUE INDEX uk_wrong_user_question
    ON wrong_question(username, question_id);
