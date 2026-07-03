package ui;

import dao.QuestionDao;
import model.Question;
import service.ExamService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * 考试配置面板，用于选择题库、分类、数量、难度和知识点。
 */
public class ExamConfigPanel extends JPanel {
    private MainFrame mainFrame;
    private ExamService examService = new ExamService();
    private QuestionDao questionDao = new QuestionDao();

    private JSpinner countSpinner;
    private JSpinner durationSpinner;
    private JComboBox<String> bankBox;
    private JComboBox<String> categoryBox;
    private JComboBox<String> difficultyBox;
    private JComboBox<String> knowledgeBox;

    public ExamConfigPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initView();
        loadBankOptions();
    }

    private void initView() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JLabel titleLabel = new JLabel("随机组卷");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        titleLabel.setForeground(new Color(40, 53, 72));
        add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 8, 12, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        bankBox = new JComboBox<String>(new String[]{"不限"});
        categoryBox = new JComboBox<String>(new String[]{"不限"});
        countSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        durationSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 180, 1));
        difficultyBox = new JComboBox<String>(new String[]{"不限", "简单", "中等", "困难"});
        knowledgeBox = new JComboBox<String>(new String[]{"不限", "Java基础", "面向对象", "集合", "JDBC", "Swing"});

        addRow(formPanel, gbc, 0, "题库/科目", bankBox);
        addRow(formPanel, gbc, 1, "分类", categoryBox);
        addRow(formPanel, gbc, 2, "题目数量", countSpinner);
        addRow(formPanel, gbc, 3, "考试时长（分钟）", durationSpinner);
        addRow(formPanel, gbc, 4, "题目难度", difficultyBox);
        addRow(formPanel, gbc, 5, "知识点", knowledgeBox);

        JButton refreshButton = new JButton("刷新题库");
        refreshButton.addActionListener(e -> loadBankOptions());
        gbc.gridx = 1;
        gbc.gridy = 6;
        formPanel.add(refreshButton, gbc);

        JButton startButton = new JButton("生成试卷并开始答题");
        startButton.addActionListener(e -> generateExam());
        gbc.gridx = 1;
        gbc.gridy = 7;
        formPanel.add(startButton, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void loadBankOptions() {
        fillComboBox(bankBox, questionDao.getBankNames());
        fillComboBox(categoryBox, questionDao.getCategories());
    }

    private void fillComboBox(JComboBox<String> comboBox, List<String> values) {
        comboBox.removeAllItems();
        comboBox.addItem("不限");
        for (String value : values) {
            if (value != null && value.trim().length() > 0) {
                comboBox.addItem(value);
            }
        }
    }

    private void generateExam() {
        int count = (Integer) countSpinner.getValue();
        int durationMinutes = (Integer) durationSpinner.getValue();
        String bankName = getFilterValue(bankBox);
        String category = getFilterValue(categoryBox);
        String difficulty = getFilterValue(difficultyBox);
        String knowledge = getFilterValue(knowledgeBox);

        List<Question> questions = examService.generateExam(count, bankName, category, difficulty, knowledge);
        if (questions.size() == 0) {
            JOptionPane.showMessageDialog(this, "没有符合条件的题目，请先在对应题库中添加或导入题目。");
            return;
        }
        if (questions.size() < count) {
            JOptionPane.showMessageDialog(this, "符合条件的题目不足，系统将使用当前找到的 " + questions.size() + " 道题。");
        }

        mainFrame.startExam(questions, durationMinutes);
    }

    private String getFilterValue(JComboBox<String> comboBox) {
        String value = (String) comboBox.getSelectedItem();
        if (value == null || "不限".equals(value)) {
            return "";
        }
        return value;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText + "：");
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }
}
