package com.fiap.mechanical_hub.stock_service.infrastructure.http.exception;

import com.fiap.mechanical_hub.stock_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.stock_service.domain.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String BAD_REQUEST = "Requisição Inválida";

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRuleException(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "Violação de Regra de Negócio", ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Não Encontrado", ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, BAD_REQUEST, "Corpo da requisição ausente ou com formato inválido");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElement(NoSuchElementException ex) {
        return build(HttpStatus.BAD_REQUEST, BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Object> build(HttpStatus httpStatus, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(STATUS, httpStatus.value());
        body.put(ERROR, error);
        body.put(MESSAGE, message);
        return new ResponseEntity<>(body, httpStatus);
    }
}
