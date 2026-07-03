package service;

import dao.ExamRecordDao;
import dao.QuestionDao;
import dao.WrongQuestionDao;
import model.ExamRecord;
import model.Question;
import model.StudentAnswer;
import model.WrongQuestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 考试业务类，负责随机抽题、自动评分、保存考试记录和错题。
 */
public class ExamService {
    private QuestionDao questionDao = new QuestionDao();
    private ExamRecordDao examRecordDao = new ExamRecordDao();
    private WrongQuestionDao wrongQuestionDao = new WrongQuestionDao();

    public List<Question> generateExam(int count, String difficulty, String knowledge) {
        return questionDao.getRandomQuestions(count, difficulty, knowledge);
    }

    public List<Question> generateExam(int count, String bankName, String category, String difficulty, String knowledge) {
        return questionDao.getRandomQuestions(count, bankName, category, difficulty, knowledge);
    }

    public ExamRecord submitExam(String username, List<Question> questions, Map<Integer, String> answerMap) {
        ExamRecord record = new ExamRecord();
        record.setUsername(username);
        record.setBankName(getExamBankName(questions));
        record.setTotalCount(questions.size());

        int totalScore = 0;
        int correctCount = 0;
        int wrongCount = 0;

        for (Question question : questions) {
            String studentAnswer = answerMap.get(question.getId());
            boolean correct = isCorrect(question, studentAnswer);
            if (correct) {
                totalScore += question.getScore();
                correctCount++;
            } else {
                wrongCount++;
            }
        }

        record.setTotalScore(totalScore);
        record.setCorrectCount(correctCount);
        record.setWrongCount(wrongCount);

        int examRecordId = examRecordDao.addExamRecord(record);
        record.setId(examRecordId);

        if (examRecordId > 0) {
            saveAnswersAndWrongQuestions(username, examRecordId, questions, answerMap);
        }

        return record;
    }

    private String getExamBankName(List<Question> questions) {
        if (questions == null || questions.size() == 0) {
            return "综合";
        }
        String firstBank = questions.get(0).getBankName();
        for (Question question : questions) {
            if (question.getBankName() == null || !question.getBankName().equals(firstBank)) {
                return "综合";
            }
        }
        return firstBank == null || firstBank.trim().length() == 0 ? "综合" : firstBank;
    }

    private void saveAnswersAndWrongQuestions(String username, int examRecordId,
                                              List<Question> questions, Map<Integer, String> answerMap) {
        for (Question question : questions) {
            String studentAnswer = answerMap.get(question.getId());
            boolean correct = isCorrect(question, studentAnswer);

            StudentAnswer answer = new StudentAnswer();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(question.getId());
            answer.setStudentAnswer(studentAnswer == null ? "未作答" : studentAnswer);
            answer.setCorrect(correct);
            answer.setScore(correct ? question.getScore() : 0);
            examRecordDao.addStudentAnswer(answer);

            if (!correct) {
                WrongQuestion wrongQuestion = new WrongQuestion();
                wrongQuestion.setUsername(username);
                wrongQuestion.setQuestionId(question.getId());
                wrongQuestion.setWrongAnswer(studentAnswer == null ? "未作答" : studentAnswer);
                wrongQuestionDao.addWrongQuestion(wrongQuestion);
            } else {
                wrongQuestionDao.markMastered(username, question.getId());
            }
        }
    }

    private boolean isCorrect(Question question, String studentAnswer) {
        if (question.getAnswer() == null || studentAnswer == null) {
            return false;
        }
        String right = normalizeAnswer(question.getType(), question.getAnswer());
        String student = normalizeAnswer(question.getType(), studentAnswer);
        return right.equals(student);
    }

    private String normalizeAnswer(String type, String answer) {
        String text = answer == null ? "" : answer.trim().toUpperCase();
        text = text.replace("，", ",").replace("、", ",").replace(" ", "").replace(",", "");

        if ("判断题".equals(type)) {
            if ("A".equals(text) || "正确".equals(text) || "对".equals(text) || "TRUE".equals(text) || "T".equals(text)) {
                return "正确";
            }
            if ("B".equals(text) || "错误".equals(text) || "错".equals(text) || "FALSE".equals(text) || "F".equals(text)) {
                return "错误";
            }
            return text;
        }

        if ("多选题".equals(type)) {
            List<String> letters = new ArrayList<String>();
            for (int i = 0; i < text.length(); i++) {
                String letter = String.valueOf(text.charAt(i));
                if ("ABCD".contains(letter) && !letters.contains(letter)) {
                    letters.add(letter);
                }
            }
            Collections.sort(letters);
            StringBuilder builder = new StringBuilder();
            for (String letter : letters) {
                builder.append(letter);
            }
            return builder.toString();
        }

        return text.length() > 0 ? String.valueOf(text.charAt(0)) : "";
    }
}
