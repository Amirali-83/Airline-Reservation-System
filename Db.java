package com.example.airlinereservationsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Db {
    // H2 embedded file DB (lives under ./data/airline*)
    private static final String URL  = "jdbc:h2:file:./data/airline;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not on classpath", e);
        }
    }

    /** Preferred name most code expects */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Backward-compatible alias if some files use Db.get() */
    public static Connection get() throws SQLException {
        return getConnection();
    }
}
