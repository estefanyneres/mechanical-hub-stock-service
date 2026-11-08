package com.fiap.mechanical_hub.stock_service.application.dto.saga;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ExecucaoRevertidaEventTest {

    @Test
    @DisplayName("Deve expor o serviceOrderId informado no construtor")
    void shouldExposeServiceOrderId() {
        UUID serviceOrderId = UUID.randomUUID();

        ExecucaoRevertidaEvent event = new ExecucaoRevertidaEvent(serviceOrderId);

        assertThat(event.serviceOrderId()).isEqualTo(serviceOrderId);
    }
}
