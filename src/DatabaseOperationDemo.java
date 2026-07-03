import dao.QuestionDao;
import dao.UserDao;
import model.Question;
import model.User;
import util.DBUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * 数据库作业演示入口。
 * 运行后在控制台展示 JDBC 连接、查询、添加、修改和删除操作。
 */
public class DatabaseOperationDemo {
    public static void main(String[] args) {
        showConnectionInfo();
        showTableSummary();
        showUsers();
        demonstrateQuestionCrud();
    }

    private static void showConnectionInfo() {
        System.out.println("========== 1. JDBC 数据库连接 ==========");
        try (Connection conn = DBUtil.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            System.out.println("连接成功：" + !conn.isClosed());
            System.out.println("数据库：" + conn.getCatalog());
            System.out.println("数据库产品：" + metaData.getDatabaseProductName());
            System.out.println("数据库版本：" + metaData.getDatabaseProductVersion());
            System.out.println("JDBC 驱动：" + metaData.getDriverName());
        } catch (Exception e) {
            System.out.println("数据库连接失败：" + e.getMessage());
            return;
        }
    }

    private static void showTableSummary() {
        System.out.println("\n========== 2. 数据库表及记录数 ==========");
        String[] tables = {"user", "question", "exam_record", "student_answer", "wrong_question"};

        try (Connection conn = DBUtil.getConnection()) {
            for (String table : tables) {
                String sql = "SELECT COUNT(*) FROM " + table;
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println(table + " 表：共 " + rs.getInt(1) + " 条记录");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("读取表记录数失败：" + e.getMessage());
        }
    }

    private static void showUsers() {
        System.out.println("\n========== 3. JDBC 查询用户 ==========");
        UserDao userDao = new UserDao();
        List<User> users = userDao.getAllUsers();
        for (User user : users) {
            System.out.println("用户ID=" + user.getId()
                    + "，账号=" + user.getUsername()
                    + "，角色=" + user.getRole());
        }

        User loginUser = userDao.findByLogin("student", "123456", "student");
        System.out.println("数据库登录验证：" + (loginUser == null ? "失败" : "成功"));
    }

    private static void demonstrateQuestionCrud() {
        System.out.println("\n========== 4. JDBC 题目增删改查 ==========");
        QuestionDao questionDao = new QuestionDao();
        String code = "DEMO-" + System.currentTimeMillis();

        Question question = new Question();
        question.setQuestionCode(code);
        question.setBankName("JDBC演示题库");
        question.setCategory("数据库操作");
        question.setType("单选题");
        question.setContent("JDBC 中用于预编译 SQL 的接口是？");
        question.setOptionA("PreparedStatement");
        question.setOptionB("JFrame");
        question.setOptionC("ArrayList");
        question.setOptionD("Thread");
        question.setAnswer("A");
        question.setAnalysis("PreparedStatement 用于执行参数化 SQL。");
        question.setDifficulty("简单");
        question.setKnowledge("JDBC");
        question.setScore(5);

        int id = questionDao.addQuestion(question);
        System.out.println("INSERT 添加结果：id=" + id + "，编号=" + code);

        Question savedQuestion = questionDao.getQuestionById(id);
        System.out.println("SELECT 查询结果：" + savedQuestion);

        savedQuestion.setContent("JDBC 中用于执行参数化 SQL 的接口是？");
        savedQuestion.setScore(10);
        boolean updated = questionDao.updateQuestion(savedQuestion);
        System.out.println("UPDATE 修改结果：" + updated);
        System.out.println("修改后记录：" + questionDao.getQuestionById(id));

        boolean deleted = questionDao.deleteQuestion(id);
        System.out.println("DELETE 删除结果：" + deleted);
        System.out.println("删除后再次查询：" + questionDao.getQuestionById(id));
    }
}
