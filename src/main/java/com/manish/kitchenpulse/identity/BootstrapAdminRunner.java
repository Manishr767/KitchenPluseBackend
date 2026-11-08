package com.manish.kitchenpulse.identity;

import java.util.Locale;

import com.manish.kitchenpulse.common.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN so there is someone who can log in and create everyone
 * else. Runs only when BOOTSTRAP_ADMIN_EMAIL and BOOTSTRAP_ADMIN_PASSWORD are set
 * and no user with that email exists. No password is ever hard-coded in a migration.
 */
@Component
class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    BootstrapAdminRunner(UserRepository users,
                         PasswordEncoder passwordEncoder,
                         @Value("${kitchenpulse.bootstrap-admin.email:}") String email,
                         @Value("${kitchenpulse.bootstrap-admin.password:}") String password,
                         @Value("${kitchenpulse.bootstrap-admin.name:System Admin}") String name) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("Bootstrap admin not configured; skipping.");
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(normalized)) {
            return;
        }
        users.save(new User(name, normalized, passwordEncoder.encode(password), Role.ADMIN, null));
        log.info("Created bootstrap admin '{}'. Change its password after first login.", normalized);
    }
}
