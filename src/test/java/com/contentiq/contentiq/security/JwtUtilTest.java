package com.contentiq.contentiq.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String SECRET = "test-secret-that-is-long-enough-for-hs384-signing-please";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private final JwtUtil jwt = new JwtUtil(SECRET, ONE_HOUR_MS);

    @Test
    void generatedTokenIsParseable() {
        String token = jwt.generateToken("alice", "user-123");
        Claims claims = jwt.parse(token);
        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.get("uid")).isEqualTo("user-123");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void extractsUsernameAndUserId() {
        String token = jwt.generateToken("bob", "user-456");
        assertThat(jwt.extractUsername(token)).isEqualTo("bob");
        assertThat(jwt.extractUserId(token)).isEqualTo("user-456");
    }

    @Test
    void freshTokenIsValid() {
        String token = jwt.generateToken("carol", "user-789");
        assertThat(jwt.isValid(token)).isTrue();
    }

    @Test
    void malformedTokenIsInvalid() {
        assertThat(jwt.isValid("not-a-jwt")).isFalse();
        assertThat(jwt.isValid("")).isFalse();
        assertThat(jwt.isValid("a.b.c")).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretIsInvalid() {
        JwtUtil other = new JwtUtil("another-secret-also-long-enough-for-hs384-signing-bro", ONE_HOUR_MS);
        String foreignToken = other.generateToken("dave", "user-000");
        assertThat(jwt.isValid(foreignToken)).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() throws InterruptedException {
        JwtUtil shortLived = new JwtUtil(SECRET, 1L);
        String token = shortLived.generateToken("eve", "user-111");
        Thread.sleep(50);
        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    void parseThrowsOnTamperedSignature() {
        String token = jwt.generateToken("frank", "user-222");
        String tampered = token.substring(0, token.length() - 5) + "xxxxx";
        assertThatThrownBy(() -> jwt.parse(tampered))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shortSecretIsPaddedNotRejected() {
        JwtUtil shortSecret = new JwtUtil("short", ONE_HOUR_MS);
        String token = shortSecret.generateToken("grace", "user-333");
        assertThat(shortSecret.isValid(token)).isTrue();
    }

    @Test
    void expirationSecondsReflectsConfiguredMillis() {
        assertThat(jwt.getExpirationSeconds()).isEqualTo(ONE_HOUR_MS / 1000);
    }
}
