import dao.QuestionDao;
import model.Question;

import java.util.List;

/**
 * 第一阶段 DAO 测试入口。
 * 如需测试数据库增删改查，可以运行这个类。
 */
public class QuestionDaoTest {
    public static void main(String[] args) {
        QuestionDao questionDao = new QuestionDao();

        Question question = new Question();
        question.setType("单选题");
        question.setContent("Java 中哪个关键字用于定义类？");
        question.setOptionA("class");
        question.setOptionB("interface");
        question.setOptionC("public");
        question.setOptionD("static");
        question.setAnswer("A");
        question.setAnalysis("Java 使用 class 关键字定义类。");
        question.setDifficulty("简单");
        question.setKnowledge("Java基础");
        question.setScore(5);

        int newId = questionDao.addQuestion(question);
        System.out.println("添加题目成功，新题目 id = " + newId);

        System.out.println("查询所有题目：");
        List<Question> questions = questionDao.getAllQuestions();
        for (Question q : questions) {
            System.out.println(q);
        }

        Question updateQuestion = questionDao.getQuestionById(newId);
        if (updateQuestion != null) {
            updateQuestion.setContent("Java 中用于定义类的关键字是哪个？");
            updateQuestion.setScore(10);
            boolean updateResult = questionDao.updateQuestion(updateQuestion);
            System.out.println("修改题目结果：" + updateResult);
            System.out.println("修改后的题目：" + questionDao.getQuestionById(newId));
        }

        boolean deleteResult = questionDao.deleteQuestion(newId);
        System.out.println("删除题目结果：" + deleteResult);

        System.out.println("删除后剩余题目数量：" + questionDao.getAllQuestions().size());
    }
}
