package com.example.demo.config;

import org.apache.catalina.Context;
import org.apache.catalina.session.StandardManager;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * Configuration for Tomcat.
 */
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /**
     * Customize.
     *
     * @param factory factory
     */
    @Override
    public void customize(@NonNull TomcatServletWebServerFactory factory) {
        factory.addContextCustomizers((Context context) -> {
            StandardManager manager = new StandardManager();
            manager.setPathname("");
            context.setManager(manager);
        });
    }
}

