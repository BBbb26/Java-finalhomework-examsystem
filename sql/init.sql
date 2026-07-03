CREATE DATABASE IF NOT EXISTS exam_system
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- 用户表：保存管理员和学生账号。
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 题目表：保存题目、所属科目、分类、答案和分值。
CREATE TABLE IF NOT EXISTS question (
    id INT PRIMARY KEY AUTO_INCREMENT,
    question_code VARCHAR(50) UNIQUE,
    bank_name VARCHAR(100) DEFAULT '默认题库',
    category VARCHAR(100) DEFAULT '未分类',
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    optionA VARCHAR(255),
    optionB VARCHAR(255),
    optionC VARCHAR(255),
    optionD VARCHAR(255),
    answer VARCHAR(50) NOT NULL,
    analysis TEXT,
    difficulty VARCHAR(50),
    knowledge VARCHAR(100),
    score INT DEFAULT 0,
    duplicate_import_count INT NOT NULL DEFAULT 0,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,
    INDEX idx_question_bank (bank_name),
    INDEX idx_question_category (category),
    INDEX idx_question_type (type),
    INDEX idx_question_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 考试记录表：保存一次考试的汇总结果。
CREATE TABLE IF NOT EXISTS exam_record (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    bank_name VARCHAR(100) DEFAULT '综合',
    total_score INT DEFAULT 0,
    total_count INT DEFAULT 0,
    correct_count INT DEFAULT 0,
    wrong_count INT DEFAULT 0,
    exam_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exam_username (username),
    INDEX idx_exam_time (exam_time),
    CONSTRAINT fk_exam_user
        FOREIGN KEY (username) REFERENCES user(username)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 学生答题明细表：保存一次考试中每道题的作答结果。
CREATE TABLE IF NOT EXISTS student_answer (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_record_id INT NOT NULL,
    question_id INT NOT NULL,
    student_answer VARCHAR(50),
    correct TINYINT DEFAULT 0,
    score INT DEFAULT 0,
    INDEX idx_answer_exam (exam_record_id),
    INDEX idx_answer_question (question_id),
    CONSTRAINT fk_answer_exam
        FOREIGN KEY (exam_record_id) REFERENCES exam_record(id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_answer_question
        FOREIGN KEY (question_id) REFERENCES question(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 错题表：记录学生答错的题目。
CREATE TABLE IF NOT EXISTS wrong_question (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    question_id INT NOT NULL,
    wrong_answer VARCHAR(50),
    wrong_count INT NOT NULL DEFAULT 1,
    mastered TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_wrong_user_question (username, question_id),
    INDEX idx_wrong_username (username),
    INDEX idx_wrong_question (question_id),
    CONSTRAINT fk_wrong_user
        FOREIGN KEY (username) REFERENCES user(username)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_wrong_question
        FOREIGN KEY (question_id) REFERENCES question(id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 默认演示账号。课程设计中用于展示数据库登录。
INSERT IGNORE INTO user(username, password, role) VALUES
('admin', '123456', 'admin'),
('student', '123456', 'student');
