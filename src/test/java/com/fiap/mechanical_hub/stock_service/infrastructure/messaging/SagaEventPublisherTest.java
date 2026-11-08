package com.fiap.mechanical_hub.stock_service.infrastructure.messaging;

import com.fiap.mechanical_hub.stock_service.application.dto.saga.EstoqueReabastecidoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SagaEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private SagaEventPublisher publisher;

    @Test
    void shouldPublishEstoqueReabastecidoEvent() {
        UUID serviceOrderId = UUID.randomUUID();

        publisher.publishEstoqueReabastecido(serviceOrderId);

        ArgumentCaptor<EstoqueReabastecidoEvent> captor = ArgumentCaptor.forClass(EstoqueReabastecidoEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.SAGA_EXCHANGE),
                eq(RabbitMQConfig.ROUTING_KEY_ESTOQUE_REABASTECIDO),
                captor.capture());

        assertEquals(serviceOrderId, captor.getValue().serviceOrderId());
    }
}
