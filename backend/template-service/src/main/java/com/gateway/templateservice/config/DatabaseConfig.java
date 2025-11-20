package com.gateway.templateservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Configuration
    @Profile("dev")
    static class DevConfig {
        // H2 specific configuration if needed
    }

    @Configuration
    @Profile("prod")
    static class ProdConfig {
        // MSSQL specific configuration if needed
    }
}