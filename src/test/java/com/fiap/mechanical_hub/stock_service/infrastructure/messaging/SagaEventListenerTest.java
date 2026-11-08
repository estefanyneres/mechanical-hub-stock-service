package com.fiap.mechanical_hub.stock_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SagaEventListenerTest {

    @Mock
    private StockUseCase stockUseCase;

    private SagaEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SagaEventListener(stockUseCase, new ObjectMapper());
    }

    @Test
    void shouldDispatchExecucaoRevertida() throws Exception {
        UUID serviceOrderId = UUID.randomUUID();
        Message message = buildMessage(RabbitMQConfig.ROUTING_KEY_EXECUCAO_REVERTIDA,
                "{\"serviceOrderId\":\"" + serviceOrderId + "\"}");

        listener.onMessage(message);

        verify(stockUseCase).restoreReservedItems(serviceOrderId);
    }

    @Test
    void shouldThrowOnUnknownRoutingKey() {
        Message message = buildMessage("rota.desconhecida", "{\"serviceOrderId\":\"" + UUID.randomUUID() + "\"}");

        assertThrows(IllegalArgumentException.class, () -> listener.onMessage(message));
    }

    private Message buildMessage(String routingKey, String body) {
        MessageProperties properties = new MessageProperties();
        properties.setReceivedRoutingKey(routingKey);
        return new Message(body.getBytes(StandardCharsets.UTF_8), properties);
    }
}
