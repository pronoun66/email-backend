package com.siteminder.email.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

@ConfigurationProperties()
public class ApplicationProperties {

    @Autowired
    private Environment env;

    public String getProperty(String propertyName) {
        return env.getProperty(propertyName);
    }
}
