package com.fiap.mechanical_hub.stock_service.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidatorTest {

    private static final String SECRET = "test-secret-shared-between-services";

    private JwtValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new JwtValidator();
        Field secretField = JwtValidator.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(validator, SECRET);
    }

    private String tokenWith(String profile, Date expiresAt) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject("repair-service")
                .withClaim("profile", profile)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    @Test
    @DisplayName("Deve decodificar um token válido assinado com o mesmo secret")
    void decode_ShouldReturnDecodedJwt_WhenTokenIsValid() {
        String token = tokenWith("ADMINISTRATOR", Date.from(Instant.now().plusSeconds(3600)));

        DecodedJWT decoded = validator.decode(token);

        assertThat(decoded).isNotNull();
        assertThat(decoded.getSubject()).isEqualTo("repair-service");
    }

    @Test
    @DisplayName("Deve retornar null para um token expirado")
    void decode_ShouldReturnNull_WhenTokenExpired() {
        String token = tokenWith("ADMINISTRATOR", Date.from(Instant.now().minusSeconds(10)));

        DecodedJWT decoded = validator.decode(token);

        assertThat(decoded).isNull();
    }

    @Test
    @DisplayName("Deve retornar null para um token assinado com outro secret")
    void decode_ShouldReturnNull_WhenSignedWithDifferentSecret() {
        Algorithm wrongAlgorithm = Algorithm.HMAC256("outro-secret-completamente-diferente");
        String token = JWT.create()
                .withIssuer("auth-api")
                .withSubject("repair-service")
                .withExpiresAt(Date.from(Instant.now().plusSeconds(3600)))
                .sign(wrongAlgorithm);

        DecodedJWT decoded = validator.decode(token);

        assertThat(decoded).isNull();
    }

    @Test
    @DisplayName("Deve retornar null para um token malformado")
    void decode_ShouldReturnNull_WhenTokenIsMalformed() {
        DecodedJWT decoded = validator.decode("isto-nao-e-um-jwt-valido");

        assertThat(decoded).isNull();
    }

    @Test
    @DisplayName("Deve ler o claim de perfil embutido no token")
    void getProfileClaim_ShouldReturnProfile_WhenDecodedIsValid() {
        String token = tokenWith("ADMINISTRATOR", Date.from(Instant.now().plusSeconds(3600)));
        DecodedJWT decoded = validator.decode(token);

        String profile = validator.getProfileClaim(decoded);

        assertThat(profile).isEqualTo("ADMINISTRATOR");
    }

    @Test
    @DisplayName("Deve retornar null ao ler o claim de um DecodedJWT nulo")
    void getProfileClaim_ShouldReturnNull_WhenDecodedIsNull() {
        String profile = validator.getProfileClaim(null);

        assertThat(profile).isNull();
    }
}
