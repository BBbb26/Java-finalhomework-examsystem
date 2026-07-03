package ui;

import dao.QuestionDao;
import model.Question;
import util.CSVImportUtil;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;

/**
 * 题库导入面板。
 * 当前支持 CSV 文件导入，Excel 可另存为 CSV 后导入。
 */
public class QuestionImportPanel extends JPanel {
    private CSVImportUtil csvImportUtil = new CSVImportUtil();
    private QuestionDao questionDao = new QuestionDao();

    private List<Question> previewQuestions;
    private DefaultTableModel tableModel;
    private JTextArea messageArea;
    private JComboBox<String> defaultBankBox;
    private JComboBox<String> defaultCategoryBox;

    public QuestionImportPanel() {
        initView();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel titleLabel = new JLabel("题库导入");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        defaultBankBox = new JComboBox<String>();
        defaultCategoryBox = new JComboBox<String>();
        defaultBankBox.setEditable(true);
        defaultCategoryBox.setEditable(true);
        loadDefaultOptions();

        JButton chooseButton = new JButton("选择 CSV 文件");
        JButton importButton = new JButton("导入到数据库");
        chooseButton.addActionListener(e -> chooseFile());
        importButton.addActionListener(e -> importToDatabase());

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(new JLabel("默认科目："), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        buttonPanel.add(defaultBankBox, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0;
        buttonPanel.add(new JLabel("默认分类："), gbc);
        gbc.gridx = 3;
        gbc.weightx = 1;
        buttonPanel.add(defaultCategoryBox, gbc);
        gbc.gridx = 4;
        gbc.weightx = 0;
        buttonPanel.add(chooseButton, gbc);
        gbc.gridx = 5;
        buttonPanel.add(importButton, gbc);

        String[] columns = {"编号", "题库/科目", "分类", "题型", "题干", "A", "B", "C", "D", "答案", "难度", "知识点", "分值"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(30);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setRows(5);
        messageArea.setText("支持字段：编号、题库、分类、题型、题干、选项A、选项B、选项C、选项D、答案、解析、难度、知识点、分值。\n"
                + "多选题答案可写为 AC、A,C 或 A C；判断题答案可写为 正确/错误、对/错、A/B。\n"
                + "Excel 文件请先另存为 CSV 后导入。CSV 未填写题库或分类时，会使用上方默认值。");

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(buttonPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        centerPanel.add(new JScrollPane(messageArea), BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV 文件", "csv"));
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        CSVImportUtil.ImportResult importResult = csvImportUtil.parse(file);
        previewQuestions = importResult.getQuestions();
        applyDefaultBankAndCategory(previewQuestions);
        refreshPreview(previewQuestions);
        showMessages(file, importResult);
    }

    private void loadDefaultOptions() {
        defaultBankBox.removeAllItems();
        defaultCategoryBox.removeAllItems();
        addComboItem(defaultBankBox, "默认题库");
        addComboItem(defaultCategoryBox, "未分类");
        for (String bankName : questionDao.getBankNames()) {
            addComboItem(defaultBankBox, bankName);
        }
        for (String category : questionDao.getCategories()) {
            addComboItem(defaultCategoryBox, category);
        }
        defaultBankBox.setSelectedItem("默认题库");
        defaultCategoryBox.setSelectedItem("未分类");
    }

    private void applyDefaultBankAndCategory(List<Question> questions) {
        String defaultBank = getComboText(defaultBankBox, "默认题库");
        String defaultCategory = getComboText(defaultCategoryBox, "未分类");
        for (Question question : questions) {
            if (isEmpty(question.getBankName()) || "默认题库".equals(question.getBankName())) {
                question.setBankName(defaultBank);
            }
            if (isEmpty(question.getCategory()) || "未分类".equals(question.getCategory())) {
                question.setCategory(defaultCategory);
            }
        }
    }

    private void addComboItem(JComboBox<String> comboBox, String value) {
        if (isEmpty(value)) {
            return;
        }
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (value.equals(comboBox.getItemAt(i))) {
                return;
            }
        }
        comboBox.addItem(value);
    }

    private void refreshPreview(List<Question> questions) {
        tableModel.setRowCount(0);
        for (Question q : questions) {
            tableModel.addRow(new Object[]{
                    q.getDisplayCode(),
                    q.getBankName(),
                    q.getCategory(),
                    q.getType(),
                    q.getContent(),
                    q.getOptionA(),
                    q.getOptionB(),
                    q.getOptionC(),
                    q.getOptionD(),
                    q.getAnswer(),
                    q.getDifficulty(),
                    q.getKnowledge(),
                    q.getScore()
            });
        }
    }

    private void showMessages(File file, CSVImportUtil.ImportResult result) {
        StringBuilder builder = new StringBuilder();
        builder.append("文件：").append(file.getAbsolutePath()).append("\n");
        builder.append("识别成功：").append(result.getQuestions().size()).append(" 道题\n");
        builder.append("识别失败：").append(result.getErrors().size()).append(" 行\n");

        for (String error : result.getErrors()) {
            builder.append(error).append("\n");
        }
        messageArea.setText(builder.toString());
    }

    private void importToDatabase() {
        if (previewQuestions == null || previewQuestions.size() == 0) {
            JOptionPane.showMessageDialog(this, "请先选择并识别 CSV 文件。");
            return;
        }

        int successCount = 0;
        int failCount = 0;
        int duplicateCount = 0;
        for (Question question : previewQuestions) {
            if (questionDao.existsDuplicate(question)) {
                if (questionDao.incrementDuplicateImportCount(question)) {
                    duplicateCount++;
                } else {
                    failCount++;
                }
                continue;
            }
            if (questionDao.existsQuestionCode(question.getQuestionCode())) {
                question.setQuestionCode(questionDao.generateNextQuestionCode(question.getBankName()));
            }
            int id = questionDao.addQuestion(question);
            if (id > 0) {
                successCount++;
            } else {
                failCount++;
            }
        }

        String summary = "导入完成：新增 " + successCount + " 道，重复未新增 " + duplicateCount
                + " 道，失败 " + failCount + " 道。\n重复题已累计到题库的“重复导入次数”。";
        JOptionPane.showMessageDialog(this, summary);
        messageArea.append("\n" + summary);
        loadDefaultOptions();
    }

    private String getComboText(JComboBox<String> comboBox, String defaultValue) {
        Object item = comboBox.getEditor().getItem();
        if (item == null) {
            item = comboBox.getSelectedItem();
        }
        String value = item == null ? "" : item.toString().trim();
        return value.length() == 0 ? defaultValue : value;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().length() == 0;
    }
}
