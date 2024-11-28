package com.aleksgolds.common;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DBAuthService {

    private List<User> entries;

    public DBAuthService() {
        String sql = "select * from Users";
        try {
            ResultSet resultSet = ConnectionToDB.statement.executeQuery(sql);
            entries = new ArrayList<>();
            while (resultSet.next()) {
                entries.add(new User(resultSet.getString("nick"),
                        resultSet.getString("login"),
                        resultSet.getString("password")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class ConnectionToDB {
        static final String DATABASEURL = "jdbc:sqlite:cloudDatabase.db";
        static Connection connection;
        static Statement statement;

        static {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DATABASEURL);
                statement = connection.createStatement();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getNickByLoginAndPassword(String login, String pass) throws SQLException {
        String sql = "select nick from Users where login = '" + login + "' and password = '" + pass + "'";
        ResultSet resultSet = ConnectionToDB.statement.executeQuery(sql);
        while (resultSet.next()) {
            return resultSet.getString("nick");
            }
        return "";
    }
    public static void insertUser(String username, String login, String password) {
        String insert = "insert into Users values (?,?,?,?)";
        PreparedStatement insertStatement = null;
        try {
            insertStatement = ConnectionToDB.connection.prepareStatement(insert);
            insertStatement.setString(2, username);
            insertStatement.setString(3, login);
            insertStatement.setString(4, password);
            insertStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

//    public static void connect() throws SQLException {
//        try {
//           Class.forName("org.sqlite.JDBC");
//            connection = DriverManager.getConnection(DATABASEURL);
//            statement = connection.createStatement();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void disconnect() throws SQLException {
//        try {
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}
