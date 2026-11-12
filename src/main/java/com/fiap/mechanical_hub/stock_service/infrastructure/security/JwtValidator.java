package com.fiap.mechanical_hub.stock_service.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * So decodifica/valida -- este servico nunca emite token (isso e do
 * core-service, /auth/login). Mesmo JWT_SECRET e mesmo issuer
 * compartilhados entre os servicos (ver TokenService do core-service).
 * Le o perfil direto do claim "profile" embutido no token, sem consultar
 * nenhum banco de usuarios (este servico nao tem esse dado - ADR-001).
 */
@Component
public class JwtValidator {

    @Value("${api.security.jwt.secret}")
    private String secret;

    public DecodedJWT decode(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public String getProfileClaim(DecodedJWT decoded) {
        return decoded == null ? null : decoded.getClaim("profile").asString();
    }
}
