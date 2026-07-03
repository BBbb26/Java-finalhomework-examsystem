package ui;

import dao.QuestionDao;
import model.Question;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * 题库管理面板。
 * 支持按题库/分类/题型/难度/知识点筛选，并使用稳定题目编号展示。
 */
public class QuestionManagePanel extends JPanel {
    private QuestionDao questionDao = new QuestionDao();
    private DefaultTableModel tableModel;
    private JTable questionTable;

    private JTextField bankSearchField;
    private JTextField categorySearchField;
    private JTextField codeSearchField;
    private JComboBox<String> typeBox;
    private JComboBox<String> difficultyBox;
    private JTextField knowledgeField;

    public QuestionManagePanel() {
        initView();
        loadAllQuestions();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 14));
        setBackground(new Color(245, 247, 250));
        add(createSearchPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = createWhitePanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        bankSearchField = new JTextField();
        categorySearchField = new JTextField();
        codeSearchField = new JTextField();
        typeBox = new JComboBox<String>(new String[]{"全部", "单选题", "多选题", "判断题"});
        difficultyBox = new JComboBox<String>(new String[]{"全部", "简单", "中等", "困难"});
        knowledgeField = new JTextField();

        addLabel(panel, gbc, 0, "题库/科目");
        addField(panel, gbc, 1, bankSearchField);
        addLabel(panel, gbc, 2, "分类");
        addField(panel, gbc, 3, categorySearchField);
        addLabel(panel, gbc, 4, "题型");
        addField(panel, gbc, 5, typeBox);
        addLabel(panel, gbc, 6, "难度");
        addField(panel, gbc, 7, difficultyBox);

        addLabel(panel, gbc, 0, 1, "知识点");
        addField(panel, gbc, 1, 1, knowledgeField);
        addLabel(panel, gbc, 2, 1, "编号");
        addField(panel, gbc, 3, 1, codeSearchField);

        JButton searchButton = new JButton("查询");
        searchButton.addActionListener(e -> searchQuestions());
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(searchButton, gbc);

        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> resetSearch());
        gbc.gridx = 5;
        panel.add(resetButton, gbc);

        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"编号", "题库/科目", "分类", "题型", "题干", "A", "B", "C", "D", "答案",
                "难度", "知识点", "分值", "重复导入次数"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionTable = new JTable(tableModel);
        questionTable.setRowHeight(30);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        questionTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 13));

        return new JScrollPane(questionTable);
    }

    private JPanel createButtonPanel() {
        JPanel panel = createWhitePanel();
        JButton addButton = new JButton("新增题目");
        JButton updateButton = new JButton("修改题目");
        JButton deleteButton = new JButton("删除题目");
        JButton restoreButton = new JButton("恢复停用题目");
        JButton refreshButton = new JButton("刷新列表");

        addButton.addActionListener(e -> showQuestionDialog(null));
        updateButton.addActionListener(e -> updateSelectedQuestion());
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        restoreButton.addActionListener(e -> restoreDeletedQuestion());
        refreshButton.addActionListener(e -> loadAllQuestions());

        panel.add(addButton);
        panel.add(updateButton);
        panel.add(deleteButton);
        panel.add(restoreButton);
        panel.add(refreshButton);
        return panel;
    }

    private void loadAllQuestions() {
        refreshTable(questionDao.getAllQuestions());
    }

    private void searchQuestions() {
        refreshTable(questionDao.searchQuestions(
                bankSearchField.getText().trim(),
                categorySearchField.getText().trim(),
                codeSearchField.getText().trim(),
                getSelectedFilter(typeBox),
                getSelectedFilter(difficultyBox),
                knowledgeField.getText().trim()
        ));
    }

    private void resetSearch() {
        bankSearchField.setText("");
        categorySearchField.setText("");
        codeSearchField.setText("");
        typeBox.setSelectedIndex(0);
        difficultyBox.setSelectedIndex(0);
        knowledgeField.setText("");
        loadAllQuestions();
    }

    private void refreshTable(List<Question> questions) {
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
                    q.getScore(),
                    q.getDuplicateImportCount()
            });
        }
    }

    private void updateSelectedQuestion() {
        Question question = getSelectedQuestion();
        if (question == null) {
            JOptionPane.showMessageDialog(this, "请先选择要修改的题目。");
            return;
        }
        showQuestionDialog(question);
    }

    private void deleteSelectedQuestion() {
        Question question = getSelectedQuestion();
        if (question == null) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的题目。");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "确定停用题目 " + question.getDisplayCode() + " 吗？\n停用后不会参与查询和组卷，但历史记录仍会保留。",
                "确认停用", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION && questionDao.deleteQuestion(question.getId())) {
            JOptionPane.showMessageDialog(this, "题目已停用，可通过“恢复停用题目”重新启用。");
            loadAllQuestions();
        }
    }

    private void restoreDeletedQuestion() {
        List<Question> deletedQuestions = questionDao.getDeletedQuestions();
        if (deletedQuestions.size() == 0) {
            JOptionPane.showMessageDialog(this, "当前没有已停用的题目。");
            return;
        }

        JComboBox<Question> questionBox = new JComboBox<Question>();
        for (Question question : deletedQuestions) {
            questionBox.addItem(question);
        }
        questionBox.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value == null ? "" :
                    value.getDisplayCode() + " | " + value.getBankName() + " | " + value.getContent());
            if (isSelected) {
                label.setOpaque(true);
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            }
            return label;
        });

        int result = JOptionPane.showConfirmDialog(this, questionBox, "选择要恢复的题目",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        Question selected = (Question) questionBox.getSelectedItem();
        if (result == JOptionPane.OK_OPTION && selected != null && questionDao.restoreQuestion(selected.getId())) {
            JOptionPane.showMessageDialog(this, "题目已恢复。");
            loadAllQuestions();
        }
    }

    private Question getSelectedQuestion() {
        int row = questionTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String code = String.valueOf(tableModel.getValueAt(questionTable.convertRowIndexToModel(row), 0));
        for (Question question : questionDao.getAllQuestions()) {
            if (code.equals(question.getDisplayCode())) {
                return question;
            }
        }
        return null;
    }

    private void showQuestionDialog(Question oldQuestion) {
        QuestionFormPanel formPanel = new QuestionFormPanel(oldQuestion);
        String title = oldQuestion == null ? "新增题目" : "修改题目";
        int result = JOptionPane.showConfirmDialog(this, formPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Question question = formPanel.getQuestion();
        if (question == null) {
            return;
        }

        boolean success;
        if (oldQuestion == null) {
            success = questionDao.addQuestion(question) > 0;
        } else {
            question.setId(oldQuestion.getId());
            success = questionDao.updateQuestion(question);
        }

        if (success) {
            JOptionPane.showMessageDialog(this, title + "成功。");
            loadAllQuestions();
        } else {
            JOptionPane.showMessageDialog(this, title + "失败，请检查数据库连接。");
        }
    }

    private JPanel createWhitePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        return panel;
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, int x, String text) {
        addLabel(panel, gbc, x, 0, text);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int x, java.awt.Component field) {
        addField(panel, gbc, x, 0, field);
    }

    private void addLabel(JPanel panel, GridBagConstraints gbc, int x, int y, String text) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(new JLabel(text + "："), gbc);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int x, int y, java.awt.Component field) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private String getSelectedFilter(JComboBox<String> comboBox) {
        String value = (String) comboBox.getSelectedItem();
        return "全部".equals(value) ? "" : value;
    }

    /**
     * 题目表单面板。题库/科目用于大分组，分类用于细分章节或模块。
     */
    private class QuestionFormPanel extends JPanel {
        private JTextField codeInput = new JTextField(26);
        private JComboBox<String> bankInput = new JComboBox<String>();
        private JComboBox<String> categoryInput = new JComboBox<String>();
        private JComboBox<String> typeInput = new JComboBox<String>(new String[]{"单选题", "多选题", "判断题"});
        private JTextArea contentInput = new JTextArea(3, 26);
        private JTextField optionAInput = new JTextField(26);
        private JTextField optionBInput = new JTextField(26);
        private JTextField optionCInput = new JTextField(26);
        private JTextField optionDInput = new JTextField(26);
        private JTextArea analysisInput = new JTextArea(3, 26);
        private JComboBox<String> difficultyInput = new JComboBox<String>(new String[]{"简单", "中等", "困难"});
        private JTextField knowledgeInput = new JTextField(26);
        private JTextField scoreInput = new JTextField(26);

        private CardLayout answerCardLayout = new CardLayout();
        private JPanel answerPanel = new JPanel(answerCardLayout);
        private JComboBox<String> singleAnswerInput = new JComboBox<String>(new String[]{"A", "B", "C", "D"});
        private JCheckBox answerAInput = new JCheckBox("A");
        private JCheckBox answerBInput = new JCheckBox("B");
        private JCheckBox answerCInput = new JCheckBox("C");
        private JCheckBox answerDInput = new JCheckBox("D");
        private JComboBox<String> judgeAnswerInput = new JComboBox<String>(new String[]{"正确", "错误"});

        QuestionFormPanel(Question question) {
            initForm();
            if (question != null) {
                fillData(question);
            } else {
                loadBankOptions("默认题库", "未分类");
                codeInput.setText(questionDao.generateNextQuestionCode(getComboText(bankInput, "默认题库")));
                changeType();
            }
        }

        private void initForm() {
            setLayout(new GridBagLayout());
            setPreferredSize(new Dimension(600, 640));
            contentInput.setLineWrap(true);
            analysisInput.setLineWrap(true);

            JPanel multiAnswerPanel = new JPanel();
            multiAnswerPanel.add(answerAInput);
            multiAnswerPanel.add(answerBInput);
            multiAnswerPanel.add(answerCInput);
            multiAnswerPanel.add(answerDInput);
            answerPanel.add(singleAnswerInput, "single");
            answerPanel.add(multiAnswerPanel, "multi");
            answerPanel.add(judgeAnswerInput, "judge");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 6, 5, 6);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            bankInput.setEditable(true);
            categoryInput.setEditable(true);
            bankInput.addActionListener(e -> {
                String bankName = getComboText(bankInput, "默认题库");
                loadCategoryOptions(bankName, getComboText(categoryInput, "未分类"));
            });

            JButton codeButton = new JButton("自动生成编号");
            codeButton.addActionListener(e -> codeInput.setText(questionDao.generateNextQuestionCode(getComboText(bankInput, "默认题库"))));

            JPanel codePanel = new JPanel(new BorderLayout(8, 0));
            codePanel.add(codeInput, BorderLayout.CENTER);
            codePanel.add(codeButton, BorderLayout.EAST);

            addRow(gbc, 0, "题目编号", codePanel);
            addRow(gbc, 1, "题库/科目", bankInput);
            addRow(gbc, 2, "分类", categoryInput);
            addRow(gbc, 3, "题型", typeInput);
            addRow(gbc, 4, "题干", new JScrollPane(contentInput));
            addRow(gbc, 5, "选项A", optionAInput);
            addRow(gbc, 6, "选项B", optionBInput);
            addRow(gbc, 7, "选项C", optionCInput);
            addRow(gbc, 8, "选项D", optionDInput);
            addRow(gbc, 9, "答案", answerPanel);
            addRow(gbc, 10, "解析", new JScrollPane(analysisInput));
            addRow(gbc, 11, "难度", difficultyInput);
            addRow(gbc, 12, "知识点", knowledgeInput);
            addRow(gbc, 13, "分值", scoreInput);
            typeInput.addActionListener(e -> changeType());
        }

        private void addRow(GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
            gbc.gridx = 0;
            gbc.gridy = row;
            gbc.weightx = 0;
            add(new JLabel(labelText + "："), gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            add(field, gbc);
        }

        private void changeType() {
            String type = (String) typeInput.getSelectedItem();
            if ("判断题".equals(type)) {
                optionAInput.setText("正确");
                optionBInput.setText("错误");
                optionCInput.setText("");
                optionDInput.setText("");
                setOptionInputsEnabled(false);
                answerCardLayout.show(answerPanel, "judge");
            } else if ("多选题".equals(type)) {
                setOptionInputsEnabled(true);
                answerCardLayout.show(answerPanel, "multi");
            } else {
                setOptionInputsEnabled(true);
                answerCardLayout.show(answerPanel, "single");
            }
        }

        private void setOptionInputsEnabled(boolean enabled) {
            optionAInput.setEnabled(enabled);
            optionBInput.setEnabled(enabled);
            optionCInput.setEnabled(enabled);
            optionDInput.setEnabled(enabled);
        }

        private void fillData(Question question) {
            loadBankOptions(question.getBankName(), question.getCategory());
            codeInput.setText(question.getQuestionCode());
            bankInput.setSelectedItem(question.getBankName());
            categoryInput.setSelectedItem(question.getCategory());
            typeInput.setSelectedItem(question.getType());
            contentInput.setText(question.getContent());
            optionAInput.setText(question.getOptionA());
            optionBInput.setText(question.getOptionB());
            optionCInput.setText(question.getOptionC());
            optionDInput.setText(question.getOptionD());
            analysisInput.setText(question.getAnalysis());
            difficultyInput.setSelectedItem(question.getDifficulty());
            knowledgeInput.setText(question.getKnowledge());
            scoreInput.setText(String.valueOf(question.getScore()));
            changeType();
            fillAnswer(question.getType(), question.getAnswer());
        }

        private void loadBankOptions(String selectedBank, String selectedCategory) {
            bankInput.removeAllItems();
            addComboItem(bankInput, "默认题库");
            for (String bankName : questionDao.getBankNames()) {
                addComboItem(bankInput, bankName);
            }
            bankInput.setSelectedItem(defaultText(selectedBank, "默认题库"));
            loadCategoryOptions(defaultText(selectedBank, "默认题库"), selectedCategory);
        }

        private void loadCategoryOptions(String bankName, String selectedCategory) {
            categoryInput.removeAllItems();
            addComboItem(categoryInput, "未分类");
            for (String category : questionDao.getCategoriesByBank(bankName)) {
                addComboItem(categoryInput, category);
            }
            categoryInput.setSelectedItem(defaultText(selectedCategory, "未分类"));
        }

        private void addComboItem(JComboBox<String> comboBox, String value) {
            if (value == null || value.trim().length() == 0) {
                return;
            }
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                if (value.equals(comboBox.getItemAt(i))) {
                    return;
                }
            }
            comboBox.addItem(value);
        }

        private void fillAnswer(String type, String answer) {
            String text = answer == null ? "" : answer.toUpperCase();
            if ("判断题".equals(type)) {
                judgeAnswerInput.setSelectedItem(text.contains("错") || "B".equals(text) ? "错误" : "正确");
            } else if ("多选题".equals(type)) {
                answerAInput.setSelected(text.contains("A"));
                answerBInput.setSelected(text.contains("B"));
                answerCInput.setSelected(text.contains("C"));
                answerDInput.setSelected(text.contains("D"));
            } else {
                singleAnswerInput.setSelectedItem(text.length() == 0 ? "A" : String.valueOf(text.charAt(0)));
            }
        }

        private Question getQuestion() {
            String type = (String) typeInput.getSelectedItem();
            String answer = getAnswer(type);
            String scoreText = scoreInput.getText().trim();
            if (contentInput.getText().trim().length() == 0 || answer.length() == 0 || scoreText.length() == 0) {
                JOptionPane.showMessageDialog(QuestionManagePanel.this, "题干、答案、分值不能为空。");
                return null;
            }

            int score;
            try {
                score = Integer.parseInt(scoreText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(QuestionManagePanel.this, "分值必须是整数。");
                return null;
            }

            Question question = new Question();
            question.setQuestionCode(codeInput.getText().trim());
            question.setBankName(getComboText(bankInput, "默认题库"));
            question.setCategory(getComboText(categoryInput, "未分类"));
            question.setType(type);
            question.setContent(contentInput.getText().trim());
            question.setOptionA(optionAInput.getText().trim());
            question.setOptionB(optionBInput.getText().trim());
            question.setOptionC(optionCInput.getText().trim());
            question.setOptionD(optionDInput.getText().trim());
            question.setAnswer(answer);
            question.setAnalysis(analysisInput.getText().trim());
            question.setDifficulty((String) difficultyInput.getSelectedItem());
            question.setKnowledge(knowledgeInput.getText().trim());
            question.setScore(score);
            return question;
        }

        private String getAnswer(String type) {
            if ("判断题".equals(type)) {
                return (String) judgeAnswerInput.getSelectedItem();
            }
            if ("多选题".equals(type)) {
                StringBuilder builder = new StringBuilder();
                if (answerAInput.isSelected()) builder.append("A");
                if (answerBInput.isSelected()) builder.append("B");
                if (answerCInput.isSelected()) builder.append("C");
                if (answerDInput.isSelected()) builder.append("D");
                return builder.toString();
            }
            return (String) singleAnswerInput.getSelectedItem();
        }

        private String defaultText(String value, String defaultValue) {
            return value == null || value.trim().length() == 0 ? defaultValue : value.trim();
        }

        private String getComboText(JComboBox<String> comboBox, String defaultValue) {
            Object item = comboBox.getEditor().getItem();
            if (item == null) {
                item = comboBox.getSelectedItem();
            }
            return defaultText(item == null ? "" : item.toString(), defaultValue);
        }
    }
}
