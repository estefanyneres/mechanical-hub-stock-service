package com.fiap.mechanical_hub.stock_service.application.usecases;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockMovementGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockMovementUseCaseTest {

    @Mock
    private StockMovementGateway stockMovementGateway;

    @InjectMocks
    private StockMovementUseCase stockMovementUseCase;

    @Test
    @DisplayName("Deve registrar um movimento de entrada de estoque")
    void shouldRegisterStockEntryMovement() {
        UUID materialId = UUID.randomUUID();
        Integer quantity = 50;

        stockMovementUseCase.registerStockEntryMovement(materialId, quantity);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementGateway, times(1)).save(captor.capture());

        StockMovement captured = captor.getValue();
        assertThat(captured.getMaterialId()).isEqualTo(materialId);
        assertThat(captured.getQuantity()).isEqualTo(quantity);
        assertThat(captured.getMovementType()).isEqualTo("ENTRADA");
        assertThat(captured.getServiceOrderId()).isNull();
    }

    @Test
    @DisplayName("Deve registrar um movimento de reserva de estoque")
    void shouldRegisterReservationMovement() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        stockMovementUseCase.registerReservationMovement(materialId, serviceOrderId, 5);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementGateway, times(1)).save(captor.capture());

        assertThat(captor.getValue().getMovementType()).isEqualTo("RESERVADO");
        assertThat(captor.getValue().getServiceOrderId()).isEqualTo(serviceOrderId);
    }

    @Test
    @DisplayName("Deve registrar um movimento de retorno de estoque")
    void shouldRegisterStockReturnMovement() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        Integer quantity = 5;

        stockMovementUseCase.registerStockReturnMovement(materialId, serviceOrderId, quantity);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementGateway, times(1)).save(captor.capture());

        StockMovement captured = captor.getValue();
        assertThat(captured.getMaterialId()).isEqualTo(materialId);
        assertThat(captured.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(captured.getQuantity()).isEqualTo(quantity);
        assertThat(captured.getMovementType()).isEqualTo("RETORNO");
    }

    @Test
    @DisplayName("Deve registrar um movimento de saída de estoque")
    void shouldRegisterStockOutMovement() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        Integer quantity = 10;

        stockMovementUseCase.registerStockOutMovement(materialId, serviceOrderId, quantity);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementGateway, times(1)).save(captor.capture());

        assertThat(captor.getValue().getMovementType()).isEqualTo("SAÍDA");
    }

    @Test
    @DisplayName("Deve registrar um movimento de deleção de estoque")
    void shouldRegisterStockDeleteMovement() {
        UUID materialId = UUID.randomUUID();
        Integer quantity = 25;

        stockMovementUseCase.registerStockDeleteMovement(materialId, quantity);

        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementGateway, times(1)).save(captor.capture());

        StockMovement captured = captor.getValue();
        assertThat(captured.getServiceOrderId()).isNull();
        assertThat(captured.getMovementType()).isEqualTo("EXCLUIDO");
    }
}
