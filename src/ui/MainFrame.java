package ui;

import model.ExamRecord;
import model.Question;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * 系统主窗口。
 * 左侧为导航栏，右侧使用 CardLayout 显示不同功能页面。
 */
public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;

    private String username;
    private String role;

    private ExamPanel examPanel;
    private ResultPanel resultPanel;
    private WrongQuestionPanel wrongQuestionPanel;

    public MainFrame(String username, String role) {
        this.username = username;
        this.role = role;
        initFrame();
        initView();
    }

    private void initFrame() {
        setTitle("题库/考试系统");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 560));
    }

    private void initView() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(245, 247, 250));

        rootPanel.add(createTopPanel(), BorderLayout.NORTH);
        rootPanel.add(createNavPanel(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contentPanel.setBackground(new Color(245, 247, 250));

        if ("admin".equals(role)) {
            contentPanel.add(new QuestionManagePanel(), "questionManage");
            contentPanel.add(new QuestionImportPanel(), "import");
        } else {
            examPanel = new ExamPanel(this, username);
            resultPanel = new ResultPanel(username);
            wrongQuestionPanel = new WrongQuestionPanel(this, username);

            contentPanel.add(new ExamConfigPanel(this), "examConfig");
            contentPanel.add(examPanel, "exam");
            contentPanel.add(resultPanel, "result");
            contentPanel.add(wrongQuestionPanel, "wrongQuestion");
        }

        rootPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    public void startExam(List<Question> questions) {
        startExam(questions, 20);
    }

    public void startExam(List<Question> questions, int durationMinutes) {
        examPanel.setQuestions(questions, durationMinutes);
        showCard("exam");
    }

    public void showResult(ExamRecord record) {
        resultPanel.loadRecords();
        resultPanel.showLatestRecord(record);
        wrongQuestionPanel.loadWrongQuestions();
        showCard("result");
    }

    public void showCard(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JLabel titleLabel = new JLabel("题库/考试系统");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        titleLabel.setForeground(new Color(40, 53, 72));

        String roleName = "admin".equals(role) ? "管理员" : "学生";
        JLabel userLabel = new JLabel("当前用户：" + username + "（" + roleName + "）");
        userLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        userLabel.setForeground(new Color(88, 102, 120));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(userLabel, BorderLayout.EAST);
        return topPanel;
    }

    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setPreferredSize(new Dimension(190, 0));
        navPanel.setBackground(new Color(38, 50, 66));
        navPanel.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 12));
        buttonPanel.setOpaque(false);

        if ("admin".equals(role)) {
            buttonPanel.add(createNavButton("题库管理", "questionManage"));
            buttonPanel.add(createNavButton("导入题库", "import"));
        } else {
            buttonPanel.add(createNavButton("随机组卷", "examConfig"));
            buttonPanel.add(createNavButton("在线答题", "exam"));
            buttonPanel.add(createNavButton("考试结果", "result"));
            buttonPanel.add(createNavButton("错题本", "wrongQuestion"));
        }

        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        navPanel.add(buttonPanel, BorderLayout.NORTH);
        navPanel.add(logoutButton, BorderLayout.SOUTH);
        return navPanel;
    }

    private JButton createNavButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 42));
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));
        button.setFocusPainted(false);
        button.setBackground(new Color(64, 82, 105));
        button.setForeground(Color.WHITE);
        button.addActionListener(e -> {
            if ("result".equals(cardName) && resultPanel != null) {
                resultPanel.loadRecords();
            }
            if ("wrongQuestion".equals(cardName) && wrongQuestionPanel != null) {
                wrongQuestionPanel.loadWrongQuestions();
            }
            cardLayout.show(contentPanel, cardName);
        });
        return button;
    }
}
