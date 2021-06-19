package com.coolme.advanced.servlet.servlet;

import com.coolme.advanced.servlet.db.Data;
import java.io.IOException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(urlPatterns = {"/forward", "/test"})
public class ForwardServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        Context context;
        Context envContext;
        Data data = null;
        try {
            context = new InitialContext();
            envContext = (Context) context.lookup("java:comp/env");
            data = (Data) envContext.lookup("test/content");
            System.out.println(data);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        if (req.getRequestURI().contains("test")) {
            resp.getWriter().write("取得的 Data 为" + data);
        } else {
            RequestDispatcher requestDispatcher = req.getRequestDispatcher("/connection");
            requestDispatcher.forward(req, resp);
        }


    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
