package model;

/**
 * 题目实体类，对应数据库中的 question 表。
 * id 是数据库主键，不要求连续；questionCode 是展示给用户看的稳定编号。
 */
public class Question {
    private int id;
    private String questionCode;
    private String bankName;
    private String category;
    private String type;
    private String content;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String analysis;
    private String difficulty;
    private String knowledge;
    private int score;
    private int duplicateImportCount;

    public Question() {
        this.bankName = "默认题库";
        this.category = "未分类";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionCode() {
        return questionCode;
    }

    public void setQuestionCode(String questionCode) {
        this.questionCode = questionCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(String knowledge) {
        this.knowledge = knowledge;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDuplicateImportCount() {
        return duplicateImportCount;
    }

    public void setDuplicateImportCount(int duplicateImportCount) {
        this.duplicateImportCount = duplicateImportCount;
    }

    public String getDisplayCode() {
        if (questionCode != null && questionCode.trim().length() > 0) {
            return questionCode;
        }
        return String.format("Q%06d", id);
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionCode='" + getDisplayCode() + '\'' +
                ", bankName='" + bankName + '\'' +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", answer='" + answer + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", knowledge='" + knowledge + '\'' +
                ", score=" + score +
                ", duplicateImportCount=" + duplicateImportCount +
                '}';
    }
}
