package chadchat.infrastructure;

import chadchat.domain.channel.Channel;
import chadchat.domain.channel.ChannelNotFoundException;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class Database implements UserRepository, MessageRepository, ChannelRepository {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/chadchat?serverTimezone=UTC&allowPublicKeyRetrieval=true";

    //  Database credentials
    static final String USER = "chadchat";
    static final String PASS = null;

    // Database version
    private static final int version = 3;

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
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users WHERE username = ?;");
            s.setString(1, name);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
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
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
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
            while (rs.next()) {
                items.add(loadUser(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
                joinChannel(id, 1);
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
                findUser(rs.getInt("messages.user")),
                findChannel(rs.getInt("messages.channel"))
        );
    }


    @Override
    public Message findMessage(int id) throws MessageNotFoundException {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM messages WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
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
    public Message createMessage(int userid, String message, int channelid) throws MessageNotFoundException {
        int id;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO messages (message, user, channel) " +
                                    "VALUE (?,?, ?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, message);
            ps.setInt(2, userid);
            ps.setInt(3, channelid);

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
    public Iterable<Channel> findAllChannelsByUser(int userId) {
        List<Channel> channels = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users_channels INNER JOIN channels ON channels.id = channel WHERE user = ?;");
            s.setInt(1, userId);
            ResultSet rs = s.executeQuery();
            while ( rs.next()) {
                channels.add(loadChannel(rs));
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return channels;
    }

    @Override
    public Iterable<Channel> findAllChannels() {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement("SELECT * FROM channels;");
            ResultSet rs = s.executeQuery();
            ArrayList<Channel> items = new ArrayList<>();
            while (rs.next()) {
                items.add(loadChannel(rs));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Channel findChannel(int id) {
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM channels WHERE id = ?;");
            s.setInt(1, id);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
                return loadChannel(rs);
            } else {
                System.err.println("No version in properties.");
                throw new NoSuchElementException("No channel with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Channel createChannel(String name, int userid) throws ChannelNotFoundException {
        int id;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO channels (name) " +
                                    "VALUE (?);",
                            Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);

            ps.executeUpdate();


            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);

                var ps2 =
                        conn.prepareStatement(
                                "INSERT INTO users_channels (user, channel) " +
                                        "VALUE (?, ?);");
                ps2.setInt(1, userid);
                ps2.setInt(2, id);

                ps2.executeUpdate();

            } else {
                System.err.println("Something went wrong.");
                throw new RuntimeException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return findChannel(id);
    }

    @Override
    public Channel joinChannel(int userId, int channelId) throws UserExistsException {
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "INSERT INTO users_channels (user, channel) " +
                                    "VALUE (?, ?);");
            ps.setInt(1, userId);
            ps.setInt(2, channelId);

            ps.executeUpdate();

        } catch (SQLException throwables) {

            throw new UserExistsException("");

        }
        return findChannel(channelId);
    }

    @Override
    public boolean leaveChannel(int userId, int channelId) {
        boolean success = false;
        try (Connection conn = getConnection()) {
            var ps =
                    conn.prepareStatement(
                            "DELETE FROM users_channels WHERE user=? AND channel=?");
            ps.setInt(1, userId);
            ps.setInt(2, channelId);

            ps.executeUpdate();
            success = true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
      return success;
    }

    @Override
    public Channel findChannel(String name) throws ChannelNotFoundException{
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM channels WHERE name = ?;");
            s.setString(1, name);
            ResultSet rs = s.executeQuery();
            if (rs.next()) {
                return loadChannel(rs);
            } else {
                System.err.println("No version in properties.");
                throw new ChannelNotFoundException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<User> findUsersByChannel(int channelId) throws ChannelNotFoundException {
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement s = conn.prepareStatement(
                    "SELECT * FROM users_channels INNER JOIN users ON users.id = user WHERE channel = ?;");
            s.setInt(1, channelId);
            ResultSet rs = s.executeQuery();
            while ( rs.next()) {
                users.add( loadUser(rs));
            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }
}

