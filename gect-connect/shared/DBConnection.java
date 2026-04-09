package shared;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Thread-safe utility for MySQL Database connectivity.
 * Returns a new connection per request (short-lived).
 */
public class DBConnection {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/gect_connect?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "root123";

    static {
        try {
            Class.forName(DRIVER);
            DebugLogger.info("DB ? Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            DebugLogger.error("DB ? Failed to load MySQL driver: " + e.getMessage());
        }
    }

    /**
     * Returns a fresh database connection.
     * Caller must close it using try-with-resources or finally block.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        DebugLogger.info("DB ? New connection established successfully");
        return conn;
    }

    // Remove old singleton connection methods:
    // getInternalConnection(), isConnected(), closeConnection()
}