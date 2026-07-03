package util;

import model.Question;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV 导入工具类。
 * 推荐表头：编号,题库,分类,题型,题干,选项A,选项B,选项C,选项D,答案,解析,难度,知识点,分值
 */
public class CSVImportUtil {

    public ImportResult parse(File file) {
        ImportResult result = new ImportResult();
        try {
            List<List<String>> rows = parseCsv(readText(file));
            if (rows.size() == 0) {
                result.addError("文件为空。");
                return result;
            }

            Map<String, Integer> headerMap = buildHeaderMap(rows.get(0));
            int startRow = headerMap.size() > 0 ? 1 : 0;
            for (int i = startRow; i < rows.size(); i++) {
                if (isEmptyRow(rows.get(i))) {
                    continue;
                }
                RowParseResult rowResult = parseQuestion(rows.get(i), headerMap);
                if (rowResult.question == null) {
                    result.addError("第 " + (i + 1) + " 行：" + rowResult.errorMessage);
                } else {
                    result.addQuestion(rowResult.question);
                }
            }
        } catch (IOException e) {
            result.addError("读取文件失败：" + e.getMessage());
        }
        return result;
    }

    private String readText(File file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        try {
            return decode(bytes, StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            return decode(bytes, Charset.forName("GBK"));
        }
    }

    private String decode(byte[] bytes, Charset charset) throws CharacterCodingException {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        String text = decoder.decode(ByteBuffer.wrap(bytes)).toString();
        return text.length() > 0 && text.charAt(0) == '\uFEFF' ? text.substring(1) : text;
    }

    private List<List<String>> parseCsv(String text) {
        List<List<String>> rows = new ArrayList<List<String>>();
        List<String> row = new ArrayList<String>();
        StringBuilder cell = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (c == ',' && !inQuote) {
                row.add(cell.toString().trim());
                cell.setLength(0);
            } else if ((c == '\n' || c == '\r') && !inQuote) {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(cell.toString().trim());
                cell.setLength(0);
                rows.add(row);
                row = new ArrayList<String>();
            } else {
                cell.append(c);
            }
        }
        row.add(cell.toString().trim());
        if (!isEmptyRow(row)) {
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Integer> buildHeaderMap(List<String> headerRow) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (int i = 0; i < headerRow.size(); i++) {
            String key = normalizeHeader(headerRow.get(i));
            if (key.length() > 0) {
                map.put(key, i);
            }
        }
        if (!map.containsKey("content") || !map.containsKey("answer")) {
            map.clear();
        }
        return map;
    }

    private String normalizeHeader(String header) {
        String text = header == null ? "" : header.trim().toLowerCase().replace(" ", "").replace("_", "");
        if ("编号".equals(text) || "题号".equals(text) || "code".equals(text) || "questioncode".equals(text)) return "questionCode";
        if ("题库".equals(text) || "科目".equals(text) || "题库科目".equals(text) || "bank".equals(text) || "bankname".equals(text) || "subject".equals(text)) return "bankName";
        if ("分类".equals(text) || "章节".equals(text) || "category".equals(text)) return "category";
        if ("题型".equals(text) || "类型".equals(text) || "type".equals(text)) return "type";
        if ("题干".equals(text) || "题目".equals(text) || "内容".equals(text) || "content".equals(text)) return "content";
        if ("选项a".equals(text) || "optiona".equals(text) || "a".equals(text)) return "optionA";
        if ("选项b".equals(text) || "optionb".equals(text) || "b".equals(text)) return "optionB";
        if ("选项c".equals(text) || "optionc".equals(text) || "c".equals(text)) return "optionC";
        if ("选项d".equals(text) || "optiond".equals(text) || "d".equals(text)) return "optionD";
        if ("答案".equals(text) || "正确答案".equals(text) || "answer".equals(text)) return "answer";
        if ("解析".equals(text) || "答案解析".equals(text) || "analysis".equals(text)) return "analysis";
        if ("难度".equals(text) || "difficulty".equals(text)) return "difficulty";
        if ("知识点".equals(text) || "考点".equals(text) || "knowledge".equals(text)) return "knowledge";
        if ("分值".equals(text) || "score".equals(text)) return "score";
        return "";
    }

    private RowParseResult parseQuestion(List<String> row, Map<String, Integer> headerMap) {
        Question question = new Question();
        question.setQuestionCode(getValue(row, headerMap, "questionCode", 0));
        question.setBankName(defaultValue(getValue(row, headerMap, "bankName", 1), "默认题库"));
        question.setCategory(defaultValue(getValue(row, headerMap, "category", 2), "未分类"));

        int offset = headerMap.size() > 0 ? 0 : 3;
        String type = normalizeType(getValue(row, headerMap, "type", offset), getValue(row, headerMap, "answer", offset + 6));
        String answer = normalizeAnswer(type, getValue(row, headerMap, "answer", offset + 6));

        question.setType(type);
        question.setContent(getValue(row, headerMap, "content", offset + 1));
        question.setOptionA(getValue(row, headerMap, "optionA", offset + 2));
        question.setOptionB(getValue(row, headerMap, "optionB", offset + 3));
        question.setOptionC(getValue(row, headerMap, "optionC", offset + 4));
        question.setOptionD(getValue(row, headerMap, "optionD", offset + 5));
        question.setAnswer(answer);
        question.setAnalysis(getValue(row, headerMap, "analysis", offset + 7));
        question.setDifficulty(normalizeDifficulty(getValue(row, headerMap, "difficulty", offset + 8)));
        question.setKnowledge(getValue(row, headerMap, "knowledge", offset + 9));

        String scoreText = getValue(row, headerMap, "score", offset + 10);
        try {
            question.setScore(scoreText.length() == 0 ? 5 : Integer.parseInt(scoreText));
        } catch (NumberFormatException e) {
            return RowParseResult.error("分值必须是整数。");
        }

        if (question.getContent().length() == 0) return RowParseResult.error("题干不能为空。");
        if (question.getAnswer().length() == 0) return RowParseResult.error("答案不能为空或无法识别。");

        if ("判断题".equals(type)) {
            question.setOptionA("正确");
            question.setOptionB("错误");
            question.setOptionC("");
            question.setOptionD("");
        } else if (question.getOptionA().length() == 0 || question.getOptionB().length() == 0) {
            return RowParseResult.error("选择题至少需要选项A和选项B。");
        }
        return RowParseResult.success(question);
    }

    private String getValue(List<String> row, Map<String, Integer> headerMap, String key, int defaultIndex) {
        int index = headerMap.containsKey(key) ? headerMap.get(key) : defaultIndex;
        return index >= 0 && index < row.size() ? row.get(index).trim() : "";
    }

    private String normalizeType(String type, String answer) {
        String text = type == null ? "" : type.trim().toLowerCase();
        if (text.contains("多") || text.contains("multi")) return "多选题";
        if (text.contains("判") || text.contains("judge")) return "判断题";
        if (text.contains("单") || text.contains("single")) return "单选题";
        String answerText = answer == null ? "" : answer.trim();
        if ("正确".equals(answerText) || "错误".equals(answerText) || "对".equals(answerText) || "错".equals(answerText)) return "判断题";
        return answerText.replace(",", "").replace("，", "").replace("、", "").replace(" ", "").length() > 1 ? "多选题" : "单选题";
    }

    private String normalizeAnswer(String type, String answer) {
        String text = answer == null ? "" : answer.trim().toUpperCase().replace("，", "").replace("、", "").replace(",", "").replace(" ", "");
        if ("判断题".equals(type)) {
            if ("A".equals(text) || "正确".equals(text) || "对".equals(text) || "TRUE".equals(text) || "T".equals(text)) return "正确";
            if ("B".equals(text) || "错误".equals(text) || "错".equals(text) || "FALSE".equals(text) || "F".equals(text)) return "错误";
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ("ABCD".indexOf(c) >= 0 && builder.indexOf(String.valueOf(c)) < 0) builder.append(c);
        }
        return "单选题".equals(type) && builder.length() > 1 ? String.valueOf(builder.charAt(0)) : builder.toString();
    }

    private String normalizeDifficulty(String difficulty) {
        String text = difficulty == null ? "" : difficulty.trim();
        if (text.contains("难") || text.equalsIgnoreCase("hard")) return "困难";
        if (text.contains("中") || text.equalsIgnoreCase("medium")) return "中等";
        return "简单";
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.trim().length() == 0 ? defaultValue : value.trim();
    }

    private boolean isEmptyRow(List<String> row) {
        for (String cell : row) {
            if (cell != null && cell.trim().length() > 0) return false;
        }
        return true;
    }

    private static class RowParseResult {
        private Question question;
        private String errorMessage;

        static RowParseResult success(Question question) {
            RowParseResult result = new RowParseResult();
            result.question = question;
            return result;
        }

        static RowParseResult error(String message) {
            RowParseResult result = new RowParseResult();
            result.errorMessage = message;
            return result;
        }
    }

    public static class ImportResult {
        private List<Question> questions = new ArrayList<Question>();
        private List<String> errors = new ArrayList<String>();

        public void addQuestion(Question question) {
            questions.add(question);
        }

        public void addError(String error) {
            errors.add(error);
        }

        public List<Question> getQuestions() {
            return questions;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
