package ui;

import model.User;
import service.UserService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * 登录窗口，通过 UserService 调用 JDBC 查询数据库验证账号。
 */
public class LoginFrame extends JFrame {
    private UserService userService = new UserService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginFrame() {
        initFrame();
        initView();
    }

    private void initFrame() {
        setTitle("题库/考试系统 - 登录");
        setSize(460, 340);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void initView() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(245, 247, 250));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(24, 34, 24, 34));

        JLabel titleLabel = new JLabel("题库/考试系统", JLabel.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 26));
        titleLabel.setForeground(new Color(40, 53, 72));
        rootPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField("admin");
        passwordField = new JPasswordField("123456");
        roleBox = new JComboBox<String>(new String[]{"管理员", "学生"});

        addFormRow(formPanel, gbc, 0, "账号：", usernameField);
        addFormRow(formPanel, gbc, 1, "密码：", passwordField);
        addFormRow(formPanel, gbc, 2, "身份：", roleBox);

        JButton loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(120, 36));
        loginButton.setBackground(new Color(46, 123, 237));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> login());

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 6, 8);
        formPanel.add(loginButton, gbc);

        JLabel tipLabel = new JLabel("演示账号：admin/123456 或 student/123456", JLabel.CENTER);
        tipLabel.setForeground(new Color(110, 120, 135));
        tipLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        rootPanel.add(formPanel, BorderLayout.CENTER);
        rootPanel.add(tipLabel, BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 15));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        field.setPreferredSize(new Dimension(240, 34));
        panel.add(field, gbc);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedRole = (String) roleBox.getSelectedItem();

        if (username.length() == 0 || password.length() == 0) {
            JOptionPane.showMessageDialog(this, "请输入账号和密码。");
            return;
        }

        String role = "管理员".equals(selectedRole) ? "admin" : "student";
        User user = userService.login(username, password, role);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "账号、密码或身份选择错误。");
            return;
        }

        MainFrame mainFrame = new MainFrame(user.getUsername(), user.getRole());
        mainFrame.setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
