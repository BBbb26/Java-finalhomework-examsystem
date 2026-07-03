package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接工具类。
 * 使用前请确认 MySQL 已启动，并修改 USER 和 PASSWORD 为自己的账号密码。
 */
public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/exam_system?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password"; // TODO: 修改为你的 MySQL 密码

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("未找到 MySQL JDBC 驱动，请添加 mysql-connector-j.jar");
            e.printStackTrace();
        }
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
