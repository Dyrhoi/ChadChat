package chadchat.infrastructure;

import chadchat.domain.channel.Channel;
import chadchat.domain.channel.ChannelRepository;
import chadchat.domain.message.Message;
import chadchat.domain.message.MessageNotFoundException;
import chadchat.domain.message.MessageRepository;
import chadchat.domain.user.User;
import chadchat.domain.user.UserExistsException;
import chadchat.domain.user.UserNotFoundException;
import chadchat.domain.user.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class Database implements UserRepository, MessageRepository, ChannelRepository {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/chadchat?serverTimezone=UTC&allowPublicKeyRetrieval=true";

    //  Database credentials
    static final String USER = "chadchat";
    static final String PASS = null;

    // Database version
    private static final int version = 2;

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

    @Override
    public User findUser(String name) throws UserNotFoundException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ?;");
            s.setString(1, name);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                return loadUser(rs);
            } else {
                throw new UserNotFoundException(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private User loadUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("users.id"),
                rs.getString("users.username"),
                rs.getTimestamp("users._date").toLocalDateTime(),
                rs.getBytes("users.salt"),
                rs.getBytes("users.secret"));
    }
    public User findUser(int id) throws NoSuchElementException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                return loadUser(rs);
            } else {
                System.err.println("No version in properties.");
                throw new NoSuchElementException("No user with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<User> findAllUsers() {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement("SELECT * FROM users;");
            ResultSet rs = s.executeQuery();
            ArrayList<User> items = new ArrayList<>();
            while(rs.next()) {
                items.add(loadUser(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: this.
    @Override
    public Iterable<User> findAllUsersByChannel(int id) {
        return null;
    }

    @Override
    public User createUser(String name, byte[] salt, byte[] secret) throws UserExistsException {
        int id;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO users (username, salt, secret) " +
                                    "VALUE (?,?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setBytes(2, salt);
            ps.setBytes(3, secret);
            try {
                ps.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException e) {
                throw new UserExistsException(name);
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                throw new UserExistsException(name);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findUser(id);
    }


    /*


    Messages


     */

    private Message loadMessage(ResultSet rs) throws SQLException {
        return new Message(
                rs.getInt("messages.id"),
                rs.getString("messages.message"),
                findUser(rs.getInt("messages.user"))
        );
    }


    @Override
    public Message findMessage(int id) throws MessageNotFoundException {
        try(Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM messages WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if(rs.next()) {
                return loadMessage(rs);
            } else {
                System.err.println("No version in properties.");
                throw new NoSuchElementException("No message with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<Message> findAllMessagesByUser(int userid) {
        return null;
    }

    @Override
    public Iterable<Message> findAllMessagesByChannelId(int channelId) {
        return null;
    }

    @Override
    public Iterable<Message> findAllMessages() {
        return null;
    }

    @Override
    public Message createMessage(int userid, String message) throws MessageNotFoundException {
        int id;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO messages (message, user) " +
                                    "VALUE (?,?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, message);
            ps.setInt(2, userid);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                System.err.println("Something went wrong.");
                throw new RuntimeException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findMessage(id);
    }

    private Channel loadChannel(ResultSet rs) throws SQLException {
        return new Channel(
                rs.getInt("channels.id"),
                rs.getString("channels.name")
        );
    }

    @Override
    public Iterable<Channel> getAllChannels() {
        return null;
    }
}
