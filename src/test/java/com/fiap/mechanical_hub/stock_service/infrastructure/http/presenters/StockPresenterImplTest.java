package com.fiap.mechanical_hub.stock_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryResponse;
import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockPresenterImplTest {

    private final StockPresenterImpl presenter = new StockPresenterImpl();

    @Test
    @DisplayName("toSummaryList deve agrupar por material e somar disponível/reservado")
    void toSummaryList_ShouldGroupByMaterialAndSumQuantities() {
        UUID materialId = UUID.randomUUID();
        Stock available = Stock.setStockForNewMaterial(materialId);
        available.addQuantity(7);
        Stock reserved = Stock.createReservedStock(materialId, 3);

        List<StockSummaryResponse> result = presenter.toSummaryList(new StockSummaryOutput(List.of(available, reserved)));

        assertThat(result).hasSize(1);
        StockSummaryResponse response = result.get(0);
        assertThat(response.materialId()).isEqualTo(materialId);
        assertThat(response.availableQuantity()).isEqualTo(7);
        assertThat(response.reservedQuantity()).isEqualTo(3);
        assertThat(response.totalQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("toSummaryList deve retornar uma entrada por material diferente")
    void toSummaryList_ShouldReturnOneEntryPerMaterial() {
        Stock stockA = Stock.setStockForNewMaterial(UUID.randomUUID());
        Stock stockB = Stock.setStockForNewMaterial(UUID.randomUUID());

        List<StockSummaryResponse> result = presenter.toSummaryList(new StockSummaryOutput(List.of(stockA, stockB)));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("toDetailResponse deve somar disponível/reservado e mapear movimentos")
    void toDetailResponse_ShouldSumQuantitiesAndMapMovements() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        Stock available = Stock.setStockForNewMaterial(materialId);
        available.addQuantity(4);
        Stock reserved = Stock.createReservedStock(materialId, 6);
        StockMovement movement = StockMovement.registerReservation(materialId, serviceOrderId, 6);

        StockDetailResponse response = presenter.toDetailResponse(
                new StockDetailOutput(materialId, List.of(available, reserved), List.of(movement)));

        assertThat(response.materialId()).isEqualTo(materialId);
        assertThat(response.availableQuantity()).isEqualTo(4);
        assertThat(response.reservedQuantity()).isEqualTo(6);
        assertThat(response.totalQuantity()).isEqualTo(10);
        assertThat(response.movements()).hasSize(1);

        var movementResponse = response.movements().get(0);
        assertThat(movementResponse.id()).isEqualTo(movement.getId());
        assertThat(movementResponse.materialId()).isEqualTo(materialId);
        assertThat(movementResponse.serviceOrderId()).isEqualTo(serviceOrderId);
        assertThat(movementResponse.movementType()).isEqualTo(movement.getMovementType());
        assertThat(movementResponse.quantity()).isEqualTo(6);
        assertThat(movementResponse.createdAt()).isEqualTo(movement.getCreatedAt());
    }

    @Test
    @DisplayName("toDetailResponse deve lidar com lista de movimentos vazia")
    void toDetailResponse_ShouldHandleEmptyMovements() {
        UUID materialId = UUID.randomUUID();
        Stock available = Stock.setStockForNewMaterial(materialId);

        StockDetailResponse response = presenter.toDetailResponse(
                new StockDetailOutput(materialId, List.of(available), List.of()));

        assertThat(response.movements()).isEmpty();
        assertThat(response.availableQuantity()).isZero();
        assertThat(response.reservedQuantity()).isZero();
    }
}
