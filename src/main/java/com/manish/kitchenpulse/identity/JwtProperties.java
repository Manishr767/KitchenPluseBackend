package com.manish.kitchenpulse.identity;

import java.time.Duration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kitchenpulse.jwt")
@Validated
public record JwtProperties(
        @NotBlank
        @Size(min = 32, message = "kitchenpulse.jwt.secret must be at least 32 characters")
        String secret,

        @NotNull Duration accessTokenTtl) {
}
