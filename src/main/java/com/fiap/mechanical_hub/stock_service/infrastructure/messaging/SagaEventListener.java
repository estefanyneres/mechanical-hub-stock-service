package com.fiap.mechanical_hub.stock_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockUseCase;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Escuta a fila propria (um routing key so, por ora -- ver contrato
 * completo em RabbitMQConfig), lendo o JSON bruto, mesmo padrao do
 * core-service.
 */
@Component
public class SagaEventListener {

    private final StockUseCase stockUseCase;
    private final ObjectMapper objectMapper;

    public SagaEventListener(StockUseCase stockUseCase, ObjectMapper objectMapper) {
        this.stockUseCase = stockUseCase;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.STOCK_SERVICE_QUEUE)
    public void onMessage(Message message) throws IOException {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        JsonNode body = objectMapper.readTree(message.getBody());
        UUID serviceOrderId = UUID.fromString(body.get("serviceOrderId").asText());

        switch (routingKey) {
            case RabbitMQConfig.ROUTING_KEY_EXECUCAO_REVERTIDA -> stockUseCase.restoreReservedItems(serviceOrderId);
            default -> throw new IllegalArgumentException("Routing key não reconhecida: " + routingKey);
        }
    }
}
