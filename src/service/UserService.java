package service;

import dao.UserDao;
import model.User;

/**
 * 用户业务类，负责登录参数检查和数据库身份验证。
 */
public class UserService {
    private UserDao userDao = new UserDao();

    public User login(String username, String password, String role) {
        if (username == null || username.trim().length() == 0) {
            return null;
        }
        if (password == null || password.length() == 0) {
            return null;
        }
        if (!"admin".equals(role) && !"student".equals(role)) {
            return null;
        }
        return userDao.findByLogin(username.trim(), password, role);
    }
}
