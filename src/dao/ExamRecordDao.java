package dao;

import model.ExamRecord;
import model.StudentAnswer;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 考试记录 DAO，负责保存和查询考试结果。
 */
public class ExamRecordDao {

    public ExamRecordDao() {
        createTablesIfNeeded();
        addColumnIfNeeded("bank_name", "VARCHAR(100) DEFAULT '综合'");
    }

    public int addExamRecord(ExamRecord record) {
        String sql = "INSERT INTO exam_record(username, bank_name, total_score, total_count, correct_count, wrong_count) "
                + "VALUES(?,?,?,?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, record.getUsername());
            ps.setString(2, valueOrDefault(record.getBankName(), "综合"));
            ps.setInt(3, record.getTotalScore());
            ps.setInt(4, record.getTotalCount());
            ps.setInt(5, record.getCorrectCount());
            ps.setInt(6, record.getWrongCount());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addStudentAnswer(StudentAnswer answer) {
        String sql = "INSERT INTO student_answer(exam_record_id, question_id, student_answer, correct, score) "
                + "VALUES(?,?,?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, answer.getExamRecordId());
            ps.setInt(2, answer.getQuestionId());
            ps.setString(3, answer.getStudentAnswer());
            ps.setInt(4, answer.isCorrect() ? 1 : 0);
            ps.setInt(5, answer.getScore());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ExamRecord> getRecordsByUsername(String username) {
        String sql = "SELECT * FROM exam_record WHERE username = ? ORDER BY exam_time DESC";
        List<ExamRecord> records = new ArrayList<ExamRecord>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ExamRecord record = new ExamRecord();
                    record.setId(rs.getInt("id"));
                    record.setUsername(rs.getString("username"));
                    record.setBankName(valueOrDefault(rs.getString("bank_name"), "综合"));
                    record.setTotalScore(rs.getInt("total_score"));
                    record.setTotalCount(rs.getInt("total_count"));
                    record.setCorrectCount(rs.getInt("correct_count"));
                    record.setWrongCount(rs.getInt("wrong_count"));
                    record.setExamTime(rs.getTimestamp("exam_time"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    private void createTablesIfNeeded() {
        String examSql = "CREATE TABLE IF NOT EXISTS exam_record ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(50) NOT NULL,"
                + "bank_name VARCHAR(100) DEFAULT '综合',"
                + "total_score INT DEFAULT 0,"
                + "total_count INT DEFAULT 0,"
                + "correct_count INT DEFAULT 0,"
                + "wrong_count INT DEFAULT 0,"
                + "exam_time DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        String answerSql = "CREATE TABLE IF NOT EXISTS student_answer ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "exam_record_id INT NOT NULL,"
                + "question_id INT NOT NULL,"
                + "student_answer VARCHAR(50),"
                + "correct TINYINT DEFAULT 0,"
                + "score INT DEFAULT 0"
                + ")";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(examSql);
            stmt.executeUpdate(answerSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addColumnIfNeeded(String columnName, String definition) {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE exam_record ADD COLUMN " + columnName + " " + definition);
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate")) {
                e.printStackTrace();
            }
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.trim().length() == 0 ? defaultValue : value.trim();
    }
}
