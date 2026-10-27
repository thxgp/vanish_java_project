package com.vanish.web;

import com.vanish.service.impl.UserServiceImpl;
import com.vanish.util.AdminBootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Seeds the admin account at startup. */
@WebListener
public class AdminBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        AdminBootstrap.seed(new UserServiceImpl());
    }
}
