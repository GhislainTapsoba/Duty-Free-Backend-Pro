package com.djbc.dutyfree.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.djbc.dutyfree.repository")
@EnableTransactionManagement
public class ApplicationConfig {

    // Additional application-wide configurations can be added here
}