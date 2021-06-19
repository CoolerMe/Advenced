package com.coolme.advanced.servlet.listener;

import com.coolme.advanced.servlet.filter.CharsetFilter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class CoolContainerLister implements ServletContainerInitializer {

    private static final Logger LOGGER = Logger.getLogger(CoolContainerLister.class.getName());

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        ctx.addFilter("charsetFilter", CharsetFilter.class);

        LOGGER.log(Level.INFO, "onStartup");
    }
}
