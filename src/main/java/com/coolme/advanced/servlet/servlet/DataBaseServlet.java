package com.coolme.advanced.servlet.servlet;

import com.coolme.advanced.servlet.db.DatabaseManager;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/connection"})
public class DataBaseServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DataBaseServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        Connection connection = getConnectionFromDatabaseManager();

        resp.getWriter().write("得到的 Connection 为：" + connection);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        super.doPost(req, resp);
    }


    private Connection getConnectionFromDatabaseManager() {
        return DatabaseManager.getConnection();
    }


}
