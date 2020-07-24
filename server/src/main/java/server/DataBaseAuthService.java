package server;

import java.sql.*;

public class DataBaseAuthService implements  AuthService {
    private Connection connection;
    private Statement stmt;
    private PreparedStatement psInsert;
    private PreparedStatement psUpdate;

    public DataBaseAuthService() {
        try {
            connect();
            prepareAllStatement();
            updatePs();
            //clearTable();
            //fillTable();

        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT * FROM chat;");
        String nick = null;
        while (rs.next()) {
            if (rs.getString("login").equals(login)
                    && rs.getString("password").equals(password)) {
                nick = rs.getString("nickname");
                break;
            }
        }
        rs.close();
        return nick;
    }

    @Override
    public boolean registration(String login, String password, String nickname) throws SQLException {
       ResultSet rs = stmt.executeQuery("SELECT * FROM chat;");
       while (rs.next()) {
           if (rs.getString("login").equals(login)
                   && rs.getString("nickname").equals(nickname)) {
               return false;
           }
           if (rs.getString("login").equals(login)
                   && rs.getString("password").equals(password)) {
               psUpdate.setString(1, nickname);
               psUpdate.setString(2, rs.getString("nickname"));
               psUpdate.executeUpdate();
               rs.close();
               return true;
           }
       }

        psInsert.setString(1, login);
        psInsert.setString(2, password);
        psInsert.setString(3, nickname);
        psInsert.executeUpdate();
        rs.close();
        return true;
    }

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    public void disconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void fillTable() throws SQLException {
        connection.setAutoCommit(false);
        for (int i = 1; i <= 5; i++) {
            psInsert.setString(1, "l" + i);
            psInsert.setString(2, "p" + i);
            psInsert.setString(3, "nick" + i);
            psInsert.executeUpdate();
        }
        connection.setAutoCommit(true);
    }

    private void prepareAllStatement() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO chat (login, password, nickname) VALUES (?, ?, ?);");
    }

    private void updatePs() throws SQLException {
        psUpdate = connection.prepareStatement("UPDATE chat SET nickname = ? WHERE nickname = ?;");
    }

    private  void clearTable() throws SQLException {
        stmt.executeUpdate("DELETE FROM chat;");
    }

    private void insertEx() throws SQLException {
        stmt.executeUpdate("INSERT INTO chat (login, password, nickname) VALUES ('l1', 'p1', 'nick1')");
        stmt.executeUpdate("INSERT INTO chat (login, password, nickname) VALUES ('l2', 'p2', 'nick2')");
    }

}


