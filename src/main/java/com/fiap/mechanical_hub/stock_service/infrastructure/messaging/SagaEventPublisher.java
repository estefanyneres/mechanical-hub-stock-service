package com.fiap.mechanical_hub.stock_service.infrastructure.messaging;

import com.fiap.mechanical_hub.stock_service.application.dto.saga.EstoqueReabastecidoEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SagaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SagaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEstoqueReabastecido(UUID serviceOrderId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EXCHANGE, RabbitMQConfig.ROUTING_KEY_ESTOQUE_REABASTECIDO,
                new EstoqueReabastecidoEvent(serviceOrderId));
    }
}
