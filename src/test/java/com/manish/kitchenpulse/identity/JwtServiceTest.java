package com.manish.kitchenpulse.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234567890";
    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static JwtService serviceAt(Instant now, String secret, Duration ttl) {
        return new JwtService(new JwtProperties(secret, ttl), Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void generatedToken_parsesBackToTheSameUserId() {
        JwtService service = serviceAt(T0, SECRET, Duration.ofHours(1));

        String token = service.generateToken(42L);

        assertThat(service.parseUserId(token)).contains(42L);
    }

    @Test
    void tokenJustBeforeExpiry_isAccepted() {
        String token = serviceAt(T0, SECRET, Duration.ofHours(1)).generateToken(7L);

        JwtService later = serviceAt(T0.plus(Duration.ofMinutes(59)), SECRET, Duration.ofHours(1));

        assertThat(later.parseUserId(token)).contains(7L);
    }

    @Test
    void expiredToken_isRejected() {
        String token = serviceAt(T0, SECRET, Duration.ofHours(1)).generateToken(7L);

        JwtService later = serviceAt(T0.plus(Duration.ofHours(2)), SECRET, Duration.ofHours(1));

        assertThat(later.parseUserId(token)).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecret_isRejected() {
        String token = serviceAt(T0, "another-secret-another-secret-1234567890", Duration.ofHours(1))
                .generateToken(7L);

        assertThat(serviceAt(T0, SECRET, Duration.ofHours(1)).parseUserId(token)).isEmpty();
    }

    @Test
    void tokenWithTamperedPayload_isRejected() {
        JwtService service = serviceAt(T0, SECRET, Duration.ofHours(1));
        String token = service.generateToken(7L);

        String[] parts = token.split("\\.");
        char original = parts[1].charAt(2);
        char replacement = (original == 'A') ? 'B' : 'A';
        String tamperedPayload = parts[1].substring(0, 2) + replacement + parts[1].substring(3);
        String tampered = parts[0] + "." + tamperedPayload + "." + parts[2];

        assertThat(service.parseUserId(tampered)).isEmpty();
    }

    @Test
    void garbageInput_isRejected() {
        JwtService service = serviceAt(T0, SECRET, Duration.ofHours(1));

        assertThat(service.parseUserId("not-a-jwt")).isEmpty();
        assertThat(service.parseUserId("")).isEmpty();
        assertThat(service.parseUserId("a.b.c")).isEmpty();
    }
}
