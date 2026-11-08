package com.manish.kitchenpulse.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    /** Injectable clock so time-dependent code (JWT expiry) can be tested. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
