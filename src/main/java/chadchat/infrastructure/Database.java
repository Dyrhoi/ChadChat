package chadchat.infrastructure;

import chadchat.domain.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/chadchat?serverTimezone=UTC&allowPublicKeyRetrieval=true";

    //  Database credentials
    static final String USER = "chadchat";
    static final String PASS = null;

    /**
     * This is purely a data base test. Given that you have created a
     * users table in chatchad with an id and name.
     *
     * @throws ClassNotFoundException : If not found class
     * @throws SQLException : If exception.
     */

    private static void dbTest() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            var stmt = conn.createStatement();
            String sql;
            sql = "SELECT id, name FROM users";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("name"));
                System.out.println(user);
            }
        }
    }
}
