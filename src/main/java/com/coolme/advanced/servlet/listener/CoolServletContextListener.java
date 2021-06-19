package com.coolme.advanced.servlet.listener;

import com.coolme.advanced.servlet.db.DatabaseManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class CoolServletContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(
        CoolServletContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "contextInitialized");
        // TODO 初始化 H2  数据库
        DatabaseManager.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.log(Level.INFO, "contextDestroyed");
    }
}
