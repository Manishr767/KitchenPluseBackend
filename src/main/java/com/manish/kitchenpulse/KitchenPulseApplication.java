package com.manish.kitchenpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// We authenticate with our own JWT filter, so Spring Boot's auto-generated
// in-memory user (and its random password in the logs) is switched off.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class KitchenPulseApplication {

    public static void main(String[] args) {
        SpringApplication.run(KitchenPulseApplication.class, args);
    }
}
