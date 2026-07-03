package dao;

import model.Question;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目 DAO，负责 question 表的增删改查、筛选和随机抽题。
 */
public class QuestionDao {

    public QuestionDao() {
        ensureNewColumns();
    }

    public int addQuestion(Question question) {
        if (isEmpty(question.getQuestionCode())) {
            question.setQuestionCode(generateNextQuestionCode(question.getBankName()));
        }
        String sql = "INSERT INTO question(question_code, bank_name, category, type, content, optionA, optionB, optionC, optionD, "
                + "answer, analysis, difficulty, knowledge, score) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setQuestionParams(ps, question);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    if (isEmpty(question.getQuestionCode())) {
                        updateQuestionCode(id, String.format("Q%06d", id));
                    }
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean deleteQuestion(int id) {
        String sql = "UPDATE question SET is_deleted = 1, deleted_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateQuestion(Question question) {
        String sql = "UPDATE question SET question_code=?, bank_name=?, category=?, type=?, content=?, optionA=?, optionB=?, "
                + "optionC=?, optionD=?, answer=?, analysis=?, difficulty=?, knowledge=?, score=? WHERE id=? AND is_deleted=0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setQuestionParams(ps, question);
            ps.setInt(15, question.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Question getQuestionById(int id) {
        String sql = "SELECT * FROM question WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapQuestion(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Question> getAllQuestions() {
        return searchQuestions("", "", "", "", "");
    }

    public List<Question> getDeletedQuestions() {
        return queryQuestions("SELECT * FROM question WHERE is_deleted=1 ORDER BY deleted_at DESC",
                new ArrayList<String>());
    }

    public boolean restoreQuestion(int id) {
        String sql = "UPDATE question SET is_deleted=0, deleted_at=NULL WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Question> searchQuestions(String type, String difficulty, String knowledge) {
        return searchQuestions("", "", type, difficulty, knowledge);
    }

    public List<Question> searchQuestions(String bankName, String category, String type,
                                          String difficulty, String knowledge) {
        return searchQuestions(bankName, category, "", type, difficulty, knowledge);
    }

    public List<Question> searchQuestions(String bankName, String category, String questionCode, String type,
                                          String difficulty, String knowledge) {
        StringBuilder sql = new StringBuilder("SELECT * FROM question WHERE is_deleted=0");
        List<String> params = new ArrayList<String>();

        addEqualCondition(sql, params, "bank_name", bankName);
        addEqualCondition(sql, params, "category", category);
        if (!isEmpty(questionCode)) {
            sql.append(" AND question_code LIKE ?");
            params.add("%" + questionCode + "%");
        }
        addEqualCondition(sql, params, "type", type);
        addEqualCondition(sql, params, "difficulty", difficulty);
        if (!isEmpty(knowledge)) {
            sql.append(" AND knowledge LIKE ?");
            params.add("%" + knowledge + "%");
        }
        sql.append(" ORDER BY id DESC");
        return queryQuestions(sql.toString(), params);
    }

    public List<Question> getRandomQuestions(int count, String difficulty, String knowledge) {
        return getRandomQuestions(count, "", "", difficulty, knowledge);
    }

    public List<Question> getRandomQuestions(int count, String bankName, String category,
                                             String difficulty, String knowledge) {
        StringBuilder sql = new StringBuilder("SELECT * FROM question WHERE is_deleted=0");
        List<String> params = new ArrayList<String>();

        addEqualCondition(sql, params, "bank_name", bankName);
        addEqualCondition(sql, params, "category", category);
        addEqualCondition(sql, params, "difficulty", difficulty);
        if (!isEmpty(knowledge)) {
            sql.append(" AND knowledge LIKE ?");
            params.add("%" + knowledge + "%");
        }
        sql.append(" ORDER BY RAND() LIMIT ?");

        List<Question> questions = new ArrayList<Question>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ps.setInt(params.size() + 1, count);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapQuestion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public List<String> getBankNames() {
        return queryDistinct("bank_name");
    }

    public List<String> getCategories() {
        return queryDistinct("category");
    }

    public List<String> getCategoriesByBank(String bankName) {
        if (isEmpty(bankName)) {
            return getCategories();
        }

        String sql = "SELECT DISTINCT category FROM question "
                + "WHERE bank_name = ? AND is_deleted=0 AND category IS NOT NULL AND category <> '' ORDER BY category";
        List<String> values = new ArrayList<String>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bankName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 根据题库/科目生成下一个展示编号。
     * 数据库 id 不重排；展示编号用于录入和页面显示。
     */
    public String generateNextQuestionCode(String bankName) {
        String prefix = getCodePrefix(bankName);
        String sql = "SELECT question_code FROM question WHERE question_code LIKE ? ORDER BY question_code DESC";
        int maxNumber = 0;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("question_code");
                    int number = parseCodeNumber(code, prefix);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefix + "-" + String.format("%03d", maxNumber + 1);
    }

    /**
     * 按科目、题型和题干判断是否为重复题。
     */
    public boolean existsDuplicate(Question question) {
        String sql = "SELECT 1 FROM question WHERE is_deleted=0 AND bank_name=? AND type=? "
                + "AND TRIM(content)=TRIM(?) LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valueOrDefault(question.getBankName(), "默认题库"));
            ps.setString(2, question.getType());
            ps.setString(3, question.getContent());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 重复题不再新增，只给数据库中的原题累计一次重复导入次数。
     */
    public boolean incrementDuplicateImportCount(Question question) {
        String sql = "UPDATE question SET duplicate_import_count=duplicate_import_count+1 "
                + "WHERE is_deleted=0 AND bank_name=? AND type=? AND TRIM(content)=TRIM(?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valueOrDefault(question.getBankName(), "默认题库"));
            ps.setString(2, question.getType());
            ps.setString(3, question.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsQuestionCode(String questionCode) {
        if (isEmpty(questionCode)) {
            return false;
        }
        String sql = "SELECT 1 FROM question WHERE question_code=? LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, questionCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<Question> queryQuestions(String sql, List<String> params) {
        List<Question> questions = new ArrayList<Question>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(mapQuestion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    private List<String> queryDistinct(String column) {
        String sql = "SELECT DISTINCT " + column + " FROM question "
                + "WHERE is_deleted=0 AND " + column + " IS NOT NULL AND " + column + " <> '' ORDER BY " + column;
        List<String> values = new ArrayList<String>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return values;
    }

    private int parseCodeNumber(String code, String prefix) {
        if (code == null || !code.startsWith(prefix + "-")) {
            return 0;
        }
        String numberText = code.substring(prefix.length() + 1);
        try {
            return Integer.parseInt(numberText);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getCodePrefix(String bankName) {
        String text = bankName == null ? "" : bankName.trim();
        if (text.contains("Java") || text.contains("JAVA")) {
            return "JAVA";
        }
        if (text.contains("数据库") || text.toLowerCase().contains("database")) {
            return "DB";
        }
        if (text.contains("数据结构")) {
            return "DS";
        }
        if (text.contains("JDBC")) {
            return "JDBC";
        }
        if (text.length() == 0 || "默认题库".equals(text)) {
            return "Q";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toUpperCase(text.charAt(i));
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                builder.append(c);
            }
            if (builder.length() == 4) {
                break;
            }
        }
        return builder.length() == 0 ? "Q" : builder.toString();
    }

    private void setQuestionParams(PreparedStatement ps, Question question) throws SQLException {
        ps.setString(1, valueOrDefault(question.getQuestionCode(), ""));
        ps.setString(2, valueOrDefault(question.getBankName(), "默认题库"));
        ps.setString(3, valueOrDefault(question.getCategory(), "未分类"));
        ps.setString(4, question.getType());
        ps.setString(5, question.getContent());
        ps.setString(6, question.getOptionA());
        ps.setString(7, question.getOptionB());
        ps.setString(8, question.getOptionC());
        ps.setString(9, question.getOptionD());
        ps.setString(10, question.getAnswer());
        ps.setString(11, question.getAnalysis());
        ps.setString(12, question.getDifficulty());
        ps.setString(13, question.getKnowledge());
        ps.setInt(14, question.getScore());
    }

    private Question mapQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getInt("id"));
        question.setQuestionCode(rs.getString("question_code"));
        question.setBankName(valueOrDefault(rs.getString("bank_name"), "默认题库"));
        question.setCategory(valueOrDefault(rs.getString("category"), "未分类"));
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
        question.setDuplicateImportCount(rs.getInt("duplicate_import_count"));
        return question;
    }

    private void addEqualCondition(StringBuilder sql, List<String> params, String column, String value) {
        if (!isEmpty(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            params.add(value);
        }
    }

    private void updateQuestionCode(int id, String code) {
        String sql = "UPDATE question SET question_code = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureNewColumns() {
        addColumnIfNeeded("question_code", "VARCHAR(50)");
        addColumnIfNeeded("bank_name", "VARCHAR(100) DEFAULT '默认题库'");
        addColumnIfNeeded("category", "VARCHAR(100) DEFAULT '未分类'");
        addColumnIfNeeded("is_deleted", "TINYINT NOT NULL DEFAULT 0");
        addColumnIfNeeded("deleted_at", "DATETIME NULL");
        addColumnIfNeeded("duplicate_import_count", "INT NOT NULL DEFAULT 0");
    }

    private void addColumnIfNeeded(String columnName, String definition) {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE question ADD COLUMN " + columnName + " " + definition);
        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("duplicate")) {
                e.printStackTrace();
            }
        }
    }

    private String valueOrDefault(String value, String defaultValue) {
        return isEmpty(value) ? defaultValue : value.trim();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0
                || "全部".equals(value) || "不限".equals(value);
    }
}
