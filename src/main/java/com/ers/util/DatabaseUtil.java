package com.ers.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the H2 database connection and schema initialization.
 * Uses a file-based H2 database so data survives application restart.
 * Each call to getConnection() returns a new connection — callers must close it.
 */
public class DatabaseUtil {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUtil.class);

    private static final String DB_URL = "jdbc:h2:file:./data/ersdb;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    private static volatile boolean schemaInitialized = false;

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found", e);
        }
    }

    /**
     * Returns a new database connection. Callers are responsible for closing it.
     * Schema is initialized on the first connection.
     */
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            if (!schemaInitialized) {
                synchronized (DatabaseUtil.class) {
                    if (!schemaInitialized) {
                        initializeSchema(conn);
                        schemaInitialized = true;
                        log.info("Database schema initialized.");
                    }
                }
            }
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    private static void initializeSchema(Connection conn) {
        try (InputStream is = DatabaseUtil.class.getClassLoader()
                .getResourceAsStream("schema.sql")) {
            if (is == null) {
                throw new RuntimeException("schema.sql not found on classpath");
            }
            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
