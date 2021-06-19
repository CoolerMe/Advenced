package com.coolme.advanced.servlet.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private DataSource dataSource;

    public static void init() {
        Context context;
        Context envContext;
        try {
            context = new InitialContext();
            envContext = (Context) context.lookup("java:comp/env");
            INSTANCE.dataSource = (DataSource) envContext.lookup("jdbc/dataSource");
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    public static Connection getConnection() {
        Connection connection = null;
        if (INSTANCE.dataSource == null) {
            throw new NullPointerException();
        }
        try {
            connection = INSTANCE.dataSource.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, e.getCause().toString());
        }
        return connection;
    }
}
