package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockMovementResponseTest {

    @Test
    @DisplayName("Deve expor todos os campos informados no construtor")
    void shouldExposeAllFields() {
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        StockMovementResponse response = new StockMovementResponse(id, materialId, serviceOrderId, "ENTRADA", 10, createdAt);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.materialId()).isEqualTo(materialId);
        assertThat(response.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(response.movementType()).isEqualTo("ENTRADA");
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }
}
