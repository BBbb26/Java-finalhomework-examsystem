package ui;

import model.ExamRecord;
import model.Question;
import service.ExamService;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 答题面板，用于逐题答题并提交试卷。
 * 单选题和判断题使用单选按钮，多选题使用复选框。
 */
public class ExamPanel extends JPanel {
    private MainFrame mainFrame;
    private String username;
    private ExamService examService = new ExamService();

    private List<Question> questions;
    private Map<Integer, String> answerMap = new HashMap<Integer, String>();
    private int currentIndex = 0;

    private JLabel titleLabel;
    private JLabel timerLabel;
    private JTextArea questionArea;
    private JPanel optionPanel;

    private JRadioButton radioA = new JRadioButton();
    private JRadioButton radioB = new JRadioButton();
    private JRadioButton radioC = new JRadioButton();
    private JRadioButton radioD = new JRadioButton();
    private ButtonGroup radioGroup = new ButtonGroup();

    private JCheckBox checkA = new JCheckBox();
    private JCheckBox checkB = new JCheckBox();
    private JCheckBox checkC = new JCheckBox();
    private JCheckBox checkD = new JCheckBox();
    private Timer countdownTimer;
    private int remainingSeconds;
    private boolean submitted;

    public ExamPanel(MainFrame mainFrame, String username) {
        this.mainFrame = mainFrame;
        this.username = username;
        initView();
    }

    private void initView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        titleLabel = new JLabel("在线答题");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        timerLabel = new JLabel("剩余时间：--:--");
        timerLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        timerLabel.setForeground(new Color(190, 70, 60));
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(timerLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        questionArea = new JTextArea("请先在“随机组卷”中生成试卷。");
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);
        questionArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        questionArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        radioGroup.add(radioA);
        radioGroup.add(radioB);
        radioGroup.add(radioC);
        radioGroup.add(radioD);

        optionPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        optionPanel.setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);
        centerPanel.add(questionArea, BorderLayout.NORTH);
        centerPanel.add(optionPanel, BorderLayout.CENTER);

        JButton prevButton = new JButton("上一题");
        JButton nextButton = new JButton("下一题");
        JButton submitButton = new JButton("交卷");

        prevButton.addActionListener(e -> showPreviousQuestion());
        nextButton.addActionListener(e -> showNextQuestion());
        submitButton.addActionListener(e -> submitExam(false));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(submitButton);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        setOptionsEnabled(false);
    }

    public void setQuestions(List<Question> questions) {
        setQuestions(questions, 20);
    }

    public void setQuestions(List<Question> questions, int durationMinutes) {
        stopCountdown();
        this.questions = questions;
        this.answerMap.clear();
        this.currentIndex = 0;
        this.submitted = false;
        this.remainingSeconds = Math.max(1, durationMinutes) * 60;
        setOptionsEnabled(true);
        showQuestion();
        updateTimerLabel();
        countdownTimer = new Timer(1000, e -> tickCountdown());
        countdownTimer.start();
    }

    private void showQuestion() {
        if (questions == null || questions.size() == 0) {
            titleLabel.setText("在线答题");
            questionArea.setText("请先在“随机组卷”中生成试卷。");
            optionPanel.removeAll();
            setOptionsEnabled(false);
            revalidate();
            repaint();
            return;
        }

        Question question = questions.get(currentIndex);
        titleLabel.setText("在线答题（" + (currentIndex + 1) + "/" + questions.size() + "）");
        questionArea.setText(question.getContent() + "\n\n题型：" + question.getType() + "    分值：" + question.getScore());

        optionPanel.removeAll();
        if ("多选题".equals(question.getType())) {
            showMultiOptions(question);
        } else {
            showSingleOptions(question);
        }

        restoreSelectedAnswer(question);
        revalidate();
        repaint();
    }

    private void showSingleOptions(Question question) {
        radioGroup.clearSelection();

        if ("判断题".equals(question.getType())) {
            radioA.setText("A. 正确");
            radioB.setText("B. 错误");
            radioC.setText("");
            radioD.setText("");
            optionPanel.add(radioA);
            optionPanel.add(radioB);
            return;
        }

        radioA.setText("A. " + safeText(question.getOptionA()));
        radioB.setText("B. " + safeText(question.getOptionB()));
        radioC.setText("C. " + safeText(question.getOptionC()));
        radioD.setText("D. " + safeText(question.getOptionD()));
        optionPanel.add(radioA);
        optionPanel.add(radioB);
        optionPanel.add(radioC);
        optionPanel.add(radioD);
    }

    private void showMultiOptions(Question question) {
        clearCheckBoxSelection();
        checkA.setText("A. " + safeText(question.getOptionA()));
        checkB.setText("B. " + safeText(question.getOptionB()));
        checkC.setText("C. " + safeText(question.getOptionC()));
        checkD.setText("D. " + safeText(question.getOptionD()));
        optionPanel.add(checkA);
        optionPanel.add(checkB);
        optionPanel.add(checkC);
        optionPanel.add(checkD);
    }

    private void restoreSelectedAnswer(Question question) {
        String answer = answerMap.get(question.getId());
        if (answer == null) {
            return;
        }

        if ("多选题".equals(question.getType())) {
            checkA.setSelected(answer.contains("A"));
            checkB.setSelected(answer.contains("B"));
            checkC.setSelected(answer.contains("C"));
            checkD.setSelected(answer.contains("D"));
            return;
        }

        if (answer.contains("A") || answer.contains("正确")) {
            radioA.setSelected(true);
        } else if (answer.contains("B") || answer.contains("错误")) {
            radioB.setSelected(true);
        } else if (answer.contains("C")) {
            radioC.setSelected(true);
        } else if (answer.contains("D")) {
            radioD.setSelected(true);
        }
    }

    private void showPreviousQuestion() {
        if (questions == null || questions.size() == 0) {
            return;
        }
        saveCurrentAnswer();
        if (currentIndex > 0) {
            currentIndex--;
            showQuestion();
        }
    }

    private void showNextQuestion() {
        if (questions == null || questions.size() == 0) {
            return;
        }
        saveCurrentAnswer();
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showQuestion();
        }
    }

    private void submitExam(boolean automatic) {
        if (submitted) {
            return;
        }
        if (questions == null || questions.size() == 0) {
            JOptionPane.showMessageDialog(this, "请先生成试卷。");
            return;
        }

        saveCurrentAnswer();
        if (!automatic) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定交卷吗？未作答题目将按错误处理。",
                    "确认交卷",
                    JOptionPane.YES_NO_OPTION
            );
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        submitted = true;
        stopCountdown();
        ExamRecord record = examService.submitExam(username, questions, answerMap);
        if (record.getId() <= 0) {
            submitted = false;
            JOptionPane.showMessageDialog(this, "交卷失败，请检查数据库连接和表结构。");
            return;
        }

        String prefix = automatic ? "考试时间已到，系统已自动交卷！" : "交卷成功！";
        JOptionPane.showMessageDialog(this, prefix + " 本次得分：" + record.getTotalScore());
        mainFrame.showResult(record);
    }

    private void tickCountdown() {
        remainingSeconds--;
        updateTimerLabel();
        if (remainingSeconds <= 0) {
            submitExam(true);
        }
    }

    private void updateTimerLabel() {
        int minutes = Math.max(0, remainingSeconds) / 60;
        int seconds = Math.max(0, remainingSeconds) % 60;
        timerLabel.setText(String.format("剩余时间：%02d:%02d", minutes, seconds));
    }

    private void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    private void saveCurrentAnswer() {
        if (questions == null || questions.size() == 0) {
            return;
        }

        Question question = questions.get(currentIndex);
        String answer;
        if ("多选题".equals(question.getType())) {
            StringBuilder builder = new StringBuilder();
            if (checkA.isSelected()) {
                builder.append("A");
            }
            if (checkB.isSelected()) {
                builder.append("B");
            }
            if (checkC.isSelected()) {
                builder.append("C");
            }
            if (checkD.isSelected()) {
                builder.append("D");
            }
            answer = builder.toString();
        } else {
            answer = getSingleAnswer(question);
        }

        if (answer != null && answer.length() > 0) {
            answerMap.put(question.getId(), answer);
        } else {
            answerMap.remove(question.getId());
        }
    }

    private String getSingleAnswer(Question question) {
        if (radioA.isSelected()) {
            return "判断题".equals(question.getType()) ? "正确" : "A";
        }
        if (radioB.isSelected()) {
            return "判断题".equals(question.getType()) ? "错误" : "B";
        }
        if (radioC.isSelected()) {
            return "C";
        }
        if (radioD.isSelected()) {
            return "D";
        }
        return null;
    }

    private void clearCheckBoxSelection() {
        checkA.setSelected(false);
        checkB.setSelected(false);
        checkC.setSelected(false);
        checkD.setSelected(false);
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private void setOptionsEnabled(boolean enabled) {
        radioA.setEnabled(enabled);
        radioB.setEnabled(enabled);
        radioC.setEnabled(enabled);
        radioD.setEnabled(enabled);
        checkA.setEnabled(enabled);
        checkB.setEnabled(enabled);
        checkC.setEnabled(enabled);
        checkD.setEnabled(enabled);
    }
}
