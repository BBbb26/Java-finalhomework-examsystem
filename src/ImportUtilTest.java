import model.Question;
import util.CSVImportUtil;

import java.io.File;

/**
 * CSV 导入识别测试入口。
 */
public class ImportUtilTest {
    public static void main(String[] args) {
        CSVImportUtil util = new CSVImportUtil();
        CSVImportUtil.ImportResult result = util.parse(new File("data/question_template.csv"));

        System.out.println("识别成功：" + result.getQuestions().size());
        System.out.println("识别失败：" + result.getErrors().size());

        for (Question question : result.getQuestions()) {
            System.out.println(question.getType() + " | " + question.getContent() + " | " + question.getAnswer());
        }

        for (String error : result.getErrors()) {
            System.out.println(error);
        }
    }
}
