# 📝 Java 题库/考试系统

基于 **Java Swing + JDBC + MySQL** 的桌面端考试管理系统，支持题库管理、智能组卷、在线考试、错题回顾等功能。

## ✨ 功能概览

| 模块 | 说明 |
|------|------|
| 🔐 登录鉴权 | 管理员/学生双角色登录，数据库验证 |
| 📚 题库管理 | 题目增删改查，支持单选题、多选题、判断题 |
| 📥 CSV 导入 | 批量导入题目，支持重复检测与合并 |
| 📝 智能组卷 | 按题库、分类、难度、题型等条件随机抽题 |
| ✍️ 在线考试 | 计时答题、自动提交、即时判分 |
| 📊 成绩统计 | 考试记录查询、得分明细查看 |
| 🔄 错题本 | 自动收录错题，支持掌握标记与反复练习 |

## 🛠️ 技术栈

- **语言**: Java 8+
- **UI**: Swing
- **数据库**: MySQL 8.0
- **连接方式**: JDBC (mysql-connector-j)

## 🚀 快速开始

### 1. 环境准备

- 安装 [JDK 8+](https://www.oracle.com/java/technologies/downloads/)
- 安装 [MySQL 8.0](https://dev.mysql.com/downloads/mysql/)
- 将 `lib/mysql-connector-j-9.7.0.jar` 添加到 classpath（使用 VS Code 时已通过 `.vscode/settings.json` 配置）

### 2. 初始化数据库

在 MySQL 中执行初始化脚本：

```bash
mysql -u root -p < sql/init.sql
```

该脚本会：
- 创建 `exam_system` 数据库
- 创建 `user`、`question`、`exam_record`、`student_answer`、`wrong_question` 五张表
- 插入默认演示账号

### 3. 配置数据库连接

编辑 `src/util/DBUtil.java`，将 `USER` 和 `PASSWORD` 修改为你的 MySQL 账号密码：

```java
private static final String USER = "root";
private static final String PASSWORD = "your_password"; // 修改为你的密码
```

### 4. 编译运行

```bash
# 编译
javac -cp "lib/mysql-connector-j-9.7.0.jar" -d out src/**/*.java

# 运行
java -cp "out;lib/mysql-connector-j-9.7.0.jar" Main
```

或使用 VS Code 直接运行 `src/Main.java`。

### 5. 演示账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | `admin` | `123456` |
| 学生 | `student` | `123456` |

## 📁 项目结构

```
├── src/
│   ├── Main.java                 # 程序入口
│   ├── model/                    # 数据模型
│   │   ├── User.java
│   │   ├── Question.java
│   │   ├── ExamRecord.java
│   │   ├── StudentAnswer.java
│   │   └── WrongQuestion.java
│   ├── dao/                      # 数据访问层
│   │   ├── UserDao.java
│   │   ├── QuestionDao.java
│   │   ├── ExamRecordDao.java
│   │   └── WrongQuestionDao.java
│   ├── service/                  # 业务逻辑层
│   │   ├── UserService.java
│   │   ├── QuestionService.java
│   │   ├── ExamService.java
│   │   └── WrongQuestionService.java
│   ├── ui/                       # 用户界面 (Swing)
│   │   ├── LoginFrame.java       # 登录窗口
│   │   ├── MainFrame.java        # 主界面
│   │   ├── ExamPanel.java        # 考试面板
│   │   ├── ExamConfigPanel.java  # 组卷配置
│   │   ├── ResultPanel.java      # 成绩面板
│   │   ├── QuestionManagePanel.java  # 题库管理
│   │   ├── QuestionImportPanel.java  # 题目导入
│   │   └── WrongQuestionPanel.java   # 错题本
│   └── util/                     # 工具类
│       ├── DBUtil.java           # 数据库连接
│       └── CSVImportUtil.java    # CSV 导入工具
├── sql/
│   ├── init.sql                  # 建库建表脚本
│   ├── upgrade_20260608.sql
│   └── upgrade_duplicate_import_count.sql
├── data/
│   ├── question_template.csv     # 题目导入模板
│   └── java_demo_import.csv      # 示例题目数据
└── lib/
    └── mysql-connector-j-9.7.0.jar  # JDBC 驱动
```

## 📄 License

本项目仅用于课程学习与交流。
