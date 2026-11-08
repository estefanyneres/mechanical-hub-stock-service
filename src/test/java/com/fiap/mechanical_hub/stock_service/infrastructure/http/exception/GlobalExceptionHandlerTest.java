package com.fiap.mechanical_hub.stock_service.infrastructure.http.exception;

import com.fiap.mechanical_hub.stock_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.stock_service.domain.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @SuppressWarnings("unchecked")
    void handleBusinessRuleException_ShouldReturn422() {
        ResponseEntity<Object> response = handler.handleBusinessRuleException(new BusinessRuleException("regra violada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("message", "regra violada");
    }

    @Test
    void handleNotFound_ShouldReturn404() {
        ResponseEntity<Object> response = handler.handleNotFound(new NotFoundException("não encontrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void handleHttpMessageNotReadable_ShouldReturn400() {
        ResponseEntity<Object> response = handler.handleHttpMessageNotReadable(
                new HttpMessageNotReadableException("corpo invalido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleIllegalArgument_ShouldReturn400() {
        ResponseEntity<Object> response = handler.handleIllegalArgument(new IllegalArgumentException("argumento invalido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleNoSuchElement_ShouldReturn400() {
        ResponseEntity<Object> response = handler.handleNoSuchElement(new NoSuchElementException("elemento ausente"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
