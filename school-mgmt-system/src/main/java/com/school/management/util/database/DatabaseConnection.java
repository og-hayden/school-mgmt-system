package com.school.management.util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for managing MySQL database connections.
 * Handles obtaining and closing connections, statements, and result sets.
 */
public class DatabaseConnection {

    // Logger for logging database related messages and errors
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // Database connection parameters - Consider externalizing these in a real application
    private static final String DB_URL = "jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "17Issure!"; // Store securely!

    // Static block to load the MySQL JDBC driver when the class is loaded
    static {
        try {
            // Load the MySQL JDBC driver
            // The newInstance() call is deprecated but Class.forName() handles registration
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found.", e);
            // Consider throwing a runtime exception if the driver is essential and not found
             throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DatabaseConnection() {
        throw new IllegalStateException("Utility class should not be instantiated.");
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A Connection object to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        LOGGER.log(Level.INFO, "Attempting to establish database connection to {0}", DB_URL);
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        LOGGER.log(Level.INFO, "Database connection established successfully.");
        return connection;
    }

    /**
     * Closes the given database connection safely.
     * Logs any SQLException that occurs during closing.
     *
     * @param conn The Connection to close. Can be null.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                LOGGER.log(Level.INFO, "Database connection closed.");
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to close database connection.", e);
            }
        }
    }

    /**
     * Closes the given Statement safely.
     * Logs any SQLException that occurs during closing.
     *
     * @param stmt The Statement to close. Can be null.
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
                 LOGGER.log(Level.FINE, "Statement closed."); // Use FINE level for less critical logs
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to close statement.", e); // Warning might be more appropriate
            }
        }
    }

    /**
     * Closes the given ResultSet safely.
     * Logs any SQLException that occurs during closing.
     *
     * @param rs The ResultSet to close. Can be null.
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                 LOGGER.log(Level.FINE, "ResultSet closed."); // Use FINE level
            } catch (SQLException e) {
                 LOGGER.log(Level.WARNING, "Failed to close result set.", e); // Warning might be more appropriate
            }
        }
    }

    /**
     * Utility method to close Connection, Statement, and ResultSet resources safely.
     * This is often used in a finally block to ensure resources are released.
     *
     * @param conn The Connection to close (can be null).
     * @param stmt The Statement to close (can be null).
     * @param rs   The ResultSet to close (can be null).
     */
    public static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        // Close resources in reverse order of creation
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
         LOGGER.log(Level.INFO, "All specified database resources closed (ResultSet, Statement, Connection).");
    }

     /**
     * Utility method to close Connection and Statement resources safely.
     * Useful when no ResultSet is involved.
     *
     * @param conn The Connection to close (can be null).
     * @param stmt The Statement to close (can be null).
     */
    public static void closeResources(Connection conn, Statement stmt) {
        closeStatement(stmt);
        closeConnection(conn);
        LOGGER.log(Level.INFO, "Database resources closed (Statement, Connection).");
    }

    // --- Simple Connection Tester --- 

    /**
     * Simple main method to test the database connection.
     * Compile and run this file directly with the MySQL Connector/J JAR in the classpath.
     */
    public static void main(String[] args) {
        System.out.println("Attempting to connect to database...");
        Connection connection = null;
        try {
            // Explicitly load driver in main for standalone test robustness
            // Though the static block should handle this in normal app flow
            try {
                 Class.forName("com.mysql.cj.jdbc.Driver"); 
                 System.out.println("MySQL JDBC Driver loaded successfully in test.");
            } catch (ClassNotFoundException e) {
                 System.err.println("FATAL ERROR: MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) not found in classpath!");
                 System.err.println("Ensure mysql-connector-j-x.x.x.jar is included when running.");
                 System.err.println("Exception: " + e.getMessage());
                 return; // Cannot proceed without the driver
            }

            connection = getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("SUCCESS: Database connection established!");
                System.out.println("DB URL: " + DB_URL);
                // Optional: You could run a simple query here like "SELECT 1" 
                // using a statement and result set, then close them.
            } else {
                System.err.println("FAILURE: Failed to establish database connection. getConnection() returned null or closed connection.");
            }
        } catch (SQLException e) {
            System.err.println("FAILURE: Database connection failed due to SQLException.");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            System.err.println("\nCheck if:");
            System.err.println("1. MySQL server is running on localhost:3306.");
            System.err.println("2. Database 'school_management' exists.");
            System.err.println("3. User 'root' exists with the correct password ('17Issure!').");
            System.err.println("4. User 'root' has privileges to connect to 'school_management' from localhost.");
            // e.printStackTrace(); // Uncomment for full stack trace if needed
        } catch (Exception e) {
             System.err.println("FAILURE: An unexpected error occurred during connection test.");
             e.printStackTrace();
        } finally {
            System.out.println("Closing connection (if established)...");
            closeConnection(connection);
            System.out.println("Connection test finished.");
        }
    }
} 