package com.opsbytemitope;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * OpsByTemitope Application entry point.
 *
 * <p>Cloud-native task and project management platform.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
public class OpsByTemitopeApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpsByTemitopeApplication.class, args);
    }
}
