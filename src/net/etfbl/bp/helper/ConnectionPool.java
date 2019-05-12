package net.etfbl.bp.helper;

import net.etfbl.bp.Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectionPool {
    public static String URL = "jdbc:mysql://localhost:3306/nemanjaboje", USER = "student", PASSWORD = "student";
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                Main.showAlertError(e.getMessage());
            }
        }
        return connection;
    }

    public static void close() {
        if (connection != null)
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable ac : closeables)
            if (ac != null)
                try {
                    ac.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
    }
}