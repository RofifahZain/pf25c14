package Chapter5;

import java.sql.*;

public class MySqlExample {
    public static void main(String[] args) throws ClassNotFoundException {
        String host, port, databaseName, userName, password;
        host = port = databaseName = userName = password = null;
                host = "mysql-2631b478-rofifahzain131-ttt.b.aivencloud.com";
                userName = "avnadmin";
                password = "AVNS_rAEVPQSOwl0Oo7KS89g";
                databaseName = "tictactoedb";
                port = "22587";
        // JDBC allows to have nullable username and password
        if (host == null || port == null || databaseName == null) {
            System.out.println("Host, port, database information is required");
            return;
        }
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (final Connection connection =
                     DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require", userName, password);
             final Statement statement = connection.createStatement();
             final ResultSet resultSet = statement.executeQuery("SELECT version() AS version")) {

            while (resultSet.next()) {
                System.out.println("Version: " + resultSet.getString("version"));
            }
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}
