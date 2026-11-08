package com.fiap.mechanical_hub.stock_service.domain.entities;

import com.fiap.mechanical_hub.stock_service.domain.enums.StockMovementTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockMovementTest {

    @Test
    @DisplayName("buildStockEntryMovement deve criar movimento do tipo ENTRADA sem OS")
    void buildStockEntryMovement_ShouldCreateEntryMovement() {
        UUID materialId = UUID.randomUUID();

        StockMovement movement = StockMovement.buildStockEntryMovement(materialId, 10);

        assertThat(movement.getId()).isNotNull();
        assertThat(movement.getMaterialId()).isEqualTo(materialId);
        assertThat(movement.getServiceOrderId()).isNull();
        assertThat(movement.getMovementType()).isEqualTo(StockMovementTypeEnum.ENTRY.getDescription());
        assertThat(movement.getQuantity()).isEqualTo(10);
        assertThat(movement.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("registerReservation deve criar movimento do tipo RESERVADO com OS")
    void registerReservation_ShouldCreateReservedMovement() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        StockMovement movement = StockMovement.registerReservation(materialId, serviceOrderId, 3);

        assertThat(movement.getMaterialId()).isEqualTo(materialId);
        assertThat(movement.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(movement.getMovementType()).isEqualTo(StockMovementTypeEnum.RESERVED.getDescription());
        assertThat(movement.getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("registerReturn deve criar movimento do tipo RETORNO com OS")
    void registerReturn_ShouldCreateReturnMovement() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        StockMovement movement = StockMovement.registerReturn(materialId, serviceOrderId, 2);

        assertThat(movement.getMovementType()).isEqualTo(StockMovementTypeEnum.RETURN.getDescription());
        assertThat(movement.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(movement.getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("registerDelete deve criar movimento do tipo EXCLUIDO sem OS")
    void registerDelete_ShouldCreateDeleteMovement() {
        UUID materialId = UUID.randomUUID();

        StockMovement movement = StockMovement.registerDelete(materialId, 7);

        assertThat(movement.getMovementType()).isEqualTo(StockMovementTypeEnum.EXCLUDED.getDescription());
        assertThat(movement.getServiceOrderId()).isNull();
        assertThat(movement.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("registerStockOut deve criar movimento do tipo SAÍDA com OS")
    void registerStockOut_ShouldCreateOutMovement() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        StockMovement movement = StockMovement.registerStockOut(materialId, serviceOrderId, 1);

        assertThat(movement.getMovementType()).isEqualTo(StockMovementTypeEnum.OUT.getDescription());
        assertThat(movement.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(movement.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("Construtor completo deve preencher todos os campos")
    void constructor_ShouldSetAllFields() {
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        var now = java.time.LocalDateTime.now();

        StockMovement movement = new StockMovement(id, materialId, serviceOrderId, "ENTRADA", 5, now);

        assertThat(movement.getId()).isEqualTo(id);
        assertThat(movement.getMaterialId()).isEqualTo(materialId);
        assertThat(movement.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(movement.getMovementType()).isEqualTo("ENTRADA");
        assertThat(movement.getQuantity()).isEqualTo(5);
        assertThat(movement.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Construtor vazio deve criar instância sem erro")
    void emptyConstructor_ShouldCreateInstance() {
        StockMovement movement = new StockMovement();

        assertThat(movement.getId()).isNull();
    }
}
