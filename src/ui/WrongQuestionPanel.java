package ui;

import dao.QuestionDao;
import dao.WrongQuestionDao;
import model.Question;
import model.WrongQuestion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 错题本面板，支持按科目筛选，并显示答案对应的选项内容。
 */
public class WrongQuestionPanel extends JPanel {
    private MainFrame mainFrame;
    private String username;
    private WrongQuestionDao wrongQuestionDao = new WrongQuestionDao();
    private QuestionDao questionDao = new QuestionDao();
    private DefaultTableModel tableModel;
    private JLabel countLabel;
    private JComboBox<String> bankBox;
    private JComboBox<String> statusBox;
    private JTable table;
    private List<WrongQuestion> currentWrongQuestions;

    public WrongQuestionPanel(MainFrame mainFrame, String username) {
        this.mainFrame = mainFrame;
        this.username = username;
        initView();
        loadBankOptions();
        loadWrongQuestions();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel titleLabel = new JLabel("错题本");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new BorderLayout(8, 0));
        filterPanel.setOpaque(false);
        JPanel leftFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilterPanel.setOpaque(false);
        leftFilterPanel.add(new JLabel("科目："));
        bankBox = new JComboBox<String>();
        bankBox.addActionListener(e -> loadWrongQuestions());
        leftFilterPanel.add(bankBox);
        leftFilterPanel.add(new JLabel("状态："));
        statusBox = new JComboBox<String>(new String[]{"全部", "未掌握", "已掌握"});
        statusBox.addActionListener(e -> loadWrongQuestions());
        leftFilterPanel.add(statusBox);

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exportPanel.setOpaque(false);
        JButton exportCsvButton = new JButton("导出CSV");
        JButton exportTxtButton = new JButton("导出TXT");
        JButton practiceSelectedButton = new JButton("重练选中题");
        JButton practiceAllButton = new JButton("重练未掌握");
        practiceSelectedButton.addActionListener(e -> practiceSelectedQuestion());
        practiceAllButton.addActionListener(e -> practiceUnmasteredQuestions());
        exportCsvButton.addActionListener(e -> exportWrongQuestions("csv"));
        exportTxtButton.addActionListener(e -> exportWrongQuestions("txt"));
        exportPanel.add(practiceSelectedButton);
        exportPanel.add(practiceAllButton);
        exportPanel.add(exportCsvButton);
        exportPanel.add(exportTxtButton);

        filterPanel.add(leftFilterPanel, BorderLayout.CENTER);
        filterPanel.add(exportPanel, BorderLayout.EAST);

        String[] columns = {"最近错题时间", "状态", "错误次数", "科目", "分类", "编号", "题干", "你的答案", "正确答案", "解析"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        countLabel = new JLabel("暂无错题");
        countLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        add(countLabel, BorderLayout.SOUTH);
    }

    private void loadBankOptions() {
        bankBox.removeAllItems();
        bankBox.addItem("全部");
        for (String bankName : questionDao.getBankNames()) {
            bankBox.addItem(bankName);
        }
    }

    public void loadWrongQuestions() {
        if (tableModel == null || bankBox == null) {
            return;
        }

        tableModel.setRowCount(0);
        String selectedBank = (String) bankBox.getSelectedItem();
        List<WrongQuestion> wrongQuestions = wrongQuestionDao.getWrongQuestionsByUsername(username, selectedBank);
        currentWrongQuestions = filterByStatus(wrongQuestions);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (WrongQuestion wrongQuestion : currentWrongQuestions) {
            Question question = wrongQuestion.getQuestion();
            String time = wrongQuestion.getUpdateTime() == null ? "" : format.format(wrongQuestion.getUpdateTime());
            tableModel.addRow(new Object[]{
                    time,
                    wrongQuestion.isMastered() ? "已掌握" : "未掌握",
                    wrongQuestion.getWrongCount(),
                    question.getBankName(),
                    question.getCategory(),
                    question.getDisplayCode(),
                    question.getContent(),
                    formatAnswer(question, wrongQuestion.getWrongAnswer()),
                    formatAnswer(question, question.getAnswer()),
                    question.getAnalysis()
            });
        }

        countLabel.setText("共 " + currentWrongQuestions.size() + " 道错题。重复答错会累计次数，答对后自动标记为已掌握。");
    }

    private List<WrongQuestion> filterByStatus(List<WrongQuestion> wrongQuestions) {
        List<WrongQuestion> result = new ArrayList<WrongQuestion>();
        String status = statusBox == null ? "全部" : (String) statusBox.getSelectedItem();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            if ("未掌握".equals(status) && wrongQuestion.isMastered()) {
                continue;
            }
            if ("已掌握".equals(status) && !wrongQuestion.isMastered()) {
                continue;
            }
            result.add(wrongQuestion);
        }
        return result;
    }

    private void practiceSelectedQuestion() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一道错题。");
            return;
        }
        WrongQuestion wrongQuestion = currentWrongQuestions.get(table.convertRowIndexToModel(row));
        List<Question> questions = new ArrayList<Question>();
        questions.add(wrongQuestion.getQuestion());
        mainFrame.startExam(questions, 5);
    }

    private void practiceUnmasteredQuestions() {
        List<WrongQuestion> allWrongQuestions = wrongQuestionDao.getWrongQuestionsByUsername(username, getSelectedBank());
        List<Question> questions = new ArrayList<Question>();
        for (WrongQuestion wrongQuestion : allWrongQuestions) {
            if (!wrongQuestion.isMastered()) {
                questions.add(wrongQuestion.getQuestion());
            }
        }
        if (questions.size() == 0) {
            JOptionPane.showMessageDialog(this, "当前没有未掌握的错题。");
            return;
        }
        mainFrame.startExam(questions, Math.max(5, questions.size() * 2));
    }

    private String getSelectedBank() {
        String bank = (String) bankBox.getSelectedItem();
        return bank == null ? "全部" : bank;
    }

    private void exportWrongQuestions(String type) {
        if (currentWrongQuestions == null || currentWrongQuestions.size() == 0) {
            JOptionPane.showMessageDialog(this, "当前没有可导出的错题。");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String selectedBank = (String) bankBox.getSelectedItem();
        String bankName = selectedBank == null ? "全部" : selectedBank;
        String fileName = "错题本_" + bankName + "." + type;
        chooser.setSelectedFile(new File(fileName));
        if ("csv".equals(type)) {
            chooser.setFileFilter(new FileNameExtensionFilter("CSV 文件", "csv"));
        } else {
            chooser.setFileFilter(new FileNameExtensionFilter("TXT 文件", "txt"));
        }

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = ensureExtension(chooser.getSelectedFile(), type);
        try {
            if ("csv".equals(type)) {
                exportCsv(file);
            } else {
                exportTxt(file);
            }
            JOptionPane.showMessageDialog(this, "导出成功：" + file.getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage());
        }
    }

    private void exportCsv(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("最近错题时间,状态,错误次数,科目,分类,编号,题干,你的答案,正确答案,解析\n");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (WrongQuestion wrongQuestion : currentWrongQuestions) {
            Question question = wrongQuestion.getQuestion();
            String time = wrongQuestion.getUpdateTime() == null ? "" : format.format(wrongQuestion.getUpdateTime());
            builder.append(csv(time)).append(",");
            builder.append(csv(wrongQuestion.isMastered() ? "已掌握" : "未掌握")).append(",");
            builder.append(wrongQuestion.getWrongCount()).append(",");
            builder.append(csv(question.getBankName())).append(",");
            builder.append(csv(question.getCategory())).append(",");
            builder.append(csv(question.getDisplayCode())).append(",");
            builder.append(csv(question.getContent())).append(",");
            builder.append(csv(formatAnswer(question, wrongQuestion.getWrongAnswer()))).append(",");
            builder.append(csv(formatAnswer(question, question.getAnswer()))).append(",");
            builder.append(csv(question.getAnalysis())).append("\n");
        }

        // 写入 UTF-8 BOM，避免 Windows Excel 直接打开 CSV 时中文乱码。
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, output, 0, bom.length);
        System.arraycopy(content, 0, output, bom.length, content.length);
        Files.write(file.toPath(), output);
    }

    private void exportTxt(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        builder.append("错题本导出\n");
        builder.append("学生：").append(username).append("\n");
        builder.append("科目：").append(bankBox.getSelectedItem()).append("\n");
        builder.append("数量：").append(currentWrongQuestions.size()).append("\n\n");

        for (int i = 0; i < currentWrongQuestions.size(); i++) {
            WrongQuestion wrongQuestion = currentWrongQuestions.get(i);
            Question question = wrongQuestion.getQuestion();
            String time = wrongQuestion.getUpdateTime() == null ? "" : format.format(wrongQuestion.getUpdateTime());

            builder.append(i + 1).append(". ").append(question.getContent()).append("\n");
            builder.append("编号：").append(question.getDisplayCode()).append("\n");
            builder.append("科目：").append(question.getBankName()).append("    分类：").append(question.getCategory()).append("\n");
            builder.append("你的答案：").append(formatAnswer(question, wrongQuestion.getWrongAnswer())).append("\n");
            builder.append("正确答案：").append(formatAnswer(question, question.getAnswer())).append("\n");
            builder.append("状态：").append(wrongQuestion.isMastered() ? "已掌握" : "未掌握")
                    .append("    错误次数：").append(wrongQuestion.getWrongCount()).append("\n");
            builder.append("解析：").append(question.getAnalysis()).append("\n");
            builder.append("时间：").append(time).append("\n\n");
        }

        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            writer.write(builder.toString());
        }
    }

    private File ensureExtension(File file, String extension) {
        if (file.getName().toLowerCase().endsWith("." + extension)) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + "." + extension);
    }

    private String csv(String text) {
        String value = text == null ? "" : text;
        value = value.replace("\"", "\"\"");
        return "\"" + value + "\"";
    }

    private String formatAnswer(Question question, String answer) {
        if (answer == null || answer.trim().length() == 0 || "未作答".equals(answer)) {
            return "未作答";
        }
        if ("判断题".equals(question.getType())) {
            return answer;
        }

        StringBuilder builder = new StringBuilder();
        String text = answer.toUpperCase();
        appendOption(builder, text, "A", question.getOptionA());
        appendOption(builder, text, "B", question.getOptionB());
        appendOption(builder, text, "C", question.getOptionC());
        appendOption(builder, text, "D", question.getOptionD());
        return builder.length() == 0 ? answer : builder.toString();
    }

    private void appendOption(StringBuilder builder, String answer, String letter, String optionText) {
        if (!answer.contains(letter)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append("；");
        }
        builder.append(letter).append(". ").append(optionText == null ? "" : optionText);
    }
}
