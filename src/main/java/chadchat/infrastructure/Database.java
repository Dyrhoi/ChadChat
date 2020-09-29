package chadchat.infrastructure;

import chadchat.domain.User;

import java.sql.*;

public class Database {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/chadchat?serverTimezone=UTC&allowPublicKeyRetrieval=true";

    //  Database credentials
    static final String USER = "chadchat";
    static final String PASS = null;

    // Database version
    private static final int version = 0;

    public Database() {
        if (getCurrentVersion() != getVersion()) {
            throw new IllegalStateException("Database in wrong state, expected:"
                    + getVersion() + ", got: " + getCurrentVersion());
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static int getVersion() {
        return version;
    }

    public static int getCurrentVersion() {
        try (Connection conn = getConnection()) {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT value FROM properties WHERE name = 'version';");
            if (rs.next()) {
                String column = rs.getString("value");
                return Integer.parseInt(column);
            } else {
                System.err.println("No version in properties.");
                return -1;
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }


     /*
    public static int saveDBUser(String name) throws SQLException {
        String sql;

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)){

            var stmt = conn.createStatement();
            PreparedStatement ps = conn.prepareStatement(

                    sql = "Insert into chadchat.users (username) VALUES (?);"
            );

            ResultSet rs = stmt.executeQuery(sql);
            ps.setString(1, name);

        }
        //Få Id tilbage fra database, og return
        return 0;
    }
    public static User getUserfromDB(String name) throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            var stmt = conn.createStatement();
            String sql;
            sql = "SELECT id from chadchat.users where username = ?;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                       name);
                System.out.println(user);
                return user;
            }

        }
        return null;
    } */
}
