package dao;

import model.Question;
import model.WrongQuestion;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 错题 DAO，负责保存和查询学生错题。
 */
public class WrongQuestionDao {

    public WrongQuestionDao() {
        createTableIfNeeded();
        ensureNewColumns();
        ensureUniqueIndex();
    }

    public boolean addWrongQuestion(WrongQuestion wrongQuestion) {
        String sql = "INSERT INTO wrong_question(username, question_id, wrong_answer, wrong_count, mastered, update_time) "
                + "VALUES(?,?,?,1,0,CURRENT_TIMESTAMP) "
                + "ON DUPLICATE KEY UPDATE wrong_answer=VALUES(wrong_answer), wrong_count=wrong_count+1, "
                + "mastered=0, update_time=CURRENT_TIMESTAMP";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, wrongQuestion.getUsername());
            ps.setInt(2, wrongQuestion.getQuestionId());
            ps.setString(3, wrongQuestion.getWrongAnswer());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 学生重新练习答对后，将对应错题标记为已掌握。
     */
    public boolean markMastered(String username, int questionId) {
        String sql = "UPDATE wrong_question SET mastered=1, update_time=CURRENT_TIMESTAMP "
                + "WHERE username=? AND question_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, questionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<WrongQuestion> getWrongQuestionsByUsername(String username) {
        return getWrongQuestionsByUsername(username, "");
    }

    public List<WrongQuestion> getWrongQuestionsByUsername(String username, String bankName) {
        StringBuilder sql = new StringBuilder("SELECT w.*, q.question_code, q.bank_name, q.category, q.type, q.content, "
                + "q.optionA, q.optionB, q.optionC, q.optionD, q.answer, q.analysis, q.difficulty, q.knowledge, q.score "
                + "FROM wrong_question w JOIN question q ON w.question_id = q.id WHERE w.username = ?");
        List<String> params = new ArrayList<String>();
        params.add(username);

        if (bankName != null && bankName.trim().length() > 0 && !"全部".equals(bankName)) {
            sql.append(" AND q.bank_name = ?");
            params.add(bankName);
        }
        sql.append(" ORDER BY w.mastered ASC, w.update_time DESC");

        List<WrongQuestion> wrongQuestions = new ArrayList<WrongQuestion>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question question = new Question();
                    question.setId(rs.getInt("question_id"));
                    question.setQuestionCode(rs.getString("question_code"));
                    question.setBankName(rs.getString("bank_name"));
                    question.setCategory(rs.getString("category"));
                    question.setType(rs.getString("type"));
                    question.setContent(rs.getString("content"));
                    question.setOptionA(rs.getString("optionA"));
                    question.setOptionB(rs.getString("optionB"));
                    question.setOptionC(rs.getString("optionC"));
                    question.setOptionD(rs.getString("optionD"));
                    question.setAnswer(rs.getString("answer"));
                    question.setAnalysis(rs.getString("analysis"));
                    question.setDifficulty(rs.getString("difficulty"));
                    question.setKnowledge(rs.getString("knowledge"));
                    question.setScore(rs.getInt("score"));

                    WrongQuestion wrongQuestion = new WrongQuestion();
                    wrongQuestion.setId(rs.getInt("id"));
                    wrongQuestion.setUsername(rs.getString("username"));
                    wrongQuestion.setQuestionId(rs.getInt("question_id"));
                    wrongQuestion.setWrongAnswer(rs.getString("wrong_answer"));
                    wrongQuestion.setCreateTime(rs.getTimestamp("create_time"));
                    wrongQuestion.setUpdateTime(rs.getTimestamp("update_time"));
                    wrongQuestion.setWrongCount(rs.getInt("wrong_count"));
                    wrongQuestion.setMastered(rs.getInt("mastered") == 1);
                    wrongQuestion.setQuestion(question);
                    wrongQuestions.add(wrongQuestion);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wrongQuestions;
    }

    private void createTableIfNeeded() {
        String sql = "CREATE TABLE IF NOT EXISTS wrong_question ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(50) NOT NULL,"
                + "question_id INT NOT NULL,"
                + "wrong_answer VARCHAR(50),"
                + "wrong_count INT NOT NULL DEFAULT 1,"
                + "mastered TINYINT NOT NULL DEFAULT 0,"
                + "create_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "update_time DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureNewColumns() {
        addColumnIfNeeded("wrong_count", "INT NOT NULL DEFAULT 1");
        addColumnIfNeeded("mastered", "TINYINT NOT NULL DEFAULT 0");
        addColumnIfNeeded("update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP");
    }

    private void addColumnIfNeeded(String columnName, String definition) {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE wrong_question ADD COLUMN " + columnName + " " + definition);
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate")) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 旧数据库可能已有重复错题，先合并后再建立唯一索引。
     */
    private void ensureUniqueIndex() {
        String mergeSql = "DELETE w1 FROM wrong_question w1 JOIN wrong_question w2 "
                + "ON w1.username=w2.username AND w1.question_id=w2.question_id AND w1.id<w2.id";
        String indexSql = "CREATE UNIQUE INDEX uk_wrong_user_question ON wrong_question(username, question_id)";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(mergeSql);
            stmt.executeUpdate(indexSql);
        } catch (SQLException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (!message.contains("duplicate") && !message.contains("already exists")) {
                e.printStackTrace();
            }
        }
    }
}
