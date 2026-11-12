package com.fiap.mechanical_hub.stock_service.infrastructure.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private JwtValidator jwtValidator;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;
    @Mock
    private DecodedJWT decodedJWT;

    private SecurityFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SecurityFilter(jwtValidator);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar com ROLE_<perfil> quando o token é válido e carrega o claim de perfil")
    void doFilterInternal_ShouldAuthenticate_WhenTokenValidWithProfile() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(jwtValidator.decode("token-valido")).thenReturn(decodedJWT);
        when(jwtValidator.getProfileClaim(decodedJWT)).thenReturn("ADMINISTRATOR");
        when(decodedJWT.getSubject()).thenReturn("repair-service");

        filter.doFilterInternal(request, response, filterChain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("repair-service");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMINISTRATOR");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando não há header Authorization")
    void doFilterInternal_ShouldNotAuthenticate_WhenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o token é inválido")
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtValidator.decode("token-invalido")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o token é válido mas não carrega o claim de perfil")
    void doFilterInternal_ShouldNotAuthenticate_WhenProfileClaimMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-sem-perfil");
        when(jwtValidator.decode("token-sem-perfil")).thenReturn(decodedJWT);
        when(jwtValidator.getProfileClaim(decodedJWT)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
