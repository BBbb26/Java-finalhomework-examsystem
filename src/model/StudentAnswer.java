package model;

/**
 * 学生答题实体类，用于保存每道题的作答情况。
 */
public class StudentAnswer {
    private int id;
    private int examRecordId;
    private int questionId;
    private String studentAnswer;
    private boolean correct;
    private int score;

    public StudentAnswer() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExamRecordId() {
        return examRecordId;
    }

    public void setExamRecordId(int examRecordId) {
        this.examRecordId = examRecordId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
