// DutyFreeApplication.java
package com.djbc.dutyfree;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class DutyfreeBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(DutyfreeBackendApplication.class, args);
    }
}