import ui.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * 程序入口。
 * 启动 Swing 登录界面。
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
