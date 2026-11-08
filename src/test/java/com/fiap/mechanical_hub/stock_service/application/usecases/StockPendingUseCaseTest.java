package com.fiap.mechanical_hub.stock_service.application.usecases;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockPendingItemGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockPendingUseCaseTest {

    @Mock
    private StockPendingItemGateway repository;

    @InjectMocks
    private StockPendingUseCase stockPendingUseCase;

    @Test
    @DisplayName("Deve registrar uma nova pendência de estoque")
    void shouldRegisterPendingItem() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        Integer quantity = 10;

        stockPendingUseCase.createStockPendency(serviceOrderId, materialId, quantity);

        ArgumentCaptor<StockPendingItem> captor = ArgumentCaptor.forClass(StockPendingItem.class);
        verify(repository).save(captor.capture());

        StockPendingItem saved = captor.getValue();
        assertThat(saved.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(saved.getMaterialId()).isEqualTo(materialId);
        assertThat(saved.getQuantity()).isEqualTo(quantity);
    }

    @Test
    @DisplayName("Deve buscar pendências por ID de material")
    void shouldFindByMaterialId() {
        UUID materialId = UUID.randomUUID();
        List<StockPendingItem> mockItems = List.of(
                StockPendingItem.create(UUID.randomUUID(), 5, materialId),
                StockPendingItem.create(UUID.randomUUID(), 3, materialId)
        );

        org.mockito.Mockito.when(repository.findByMaterialIdOrderByCreatedAtAsc(materialId)).thenReturn(mockItems);

        List<StockPendingItem> result = stockPendingUseCase.findMaterialStockPendency(materialId);

        assertThat(result).hasSize(2);
        verify(repository).findByMaterialIdOrderByCreatedAtAsc(materialId);
    }

    @Test
    @DisplayName("Deve remover uma pendência de estoque")
    void shouldDeletePendingItem() {
        StockPendingItem item = StockPendingItem.create(UUID.randomUUID(), 1, UUID.randomUUID());

        stockPendingUseCase.removePendency(item);

        verify(repository, times(1)).delete(item);
    }

    @Test
    @DisplayName("Deve limpar todas as pendências de uma ordem de serviço")
    void shouldClearOrderPendencies() {
        UUID serviceOrderId = UUID.randomUUID();

        stockPendingUseCase.clearOrderPendencies(serviceOrderId);

        verify(repository, times(1)).deleteByServiceOrderId(serviceOrderId);
    }

    @Test
    @DisplayName("Deve buscar pendências por ID de ordem de serviço")
    void shouldFindByServiceOrderId() {
        UUID serviceOrderId = UUID.randomUUID();
        List<StockPendingItem> mockItems = List.of(
                StockPendingItem.create(serviceOrderId, 5, UUID.randomUUID()),
                StockPendingItem.create(serviceOrderId, 3, UUID.randomUUID())
        );

        org.mockito.Mockito.when(repository.findByServiceOrderId(serviceOrderId)).thenReturn(mockItems);

        List<StockPendingItem> result = stockPendingUseCase.findOrderStockPendency(serviceOrderId);

        assertThat(result).hasSize(2);
        verify(repository).findByServiceOrderId(serviceOrderId);
    }

    @Test
    @DisplayName("Deve atualizar a quantidade restante de uma pendência parcialmente resolvida")
    void shouldUpdatePendingItem() {
        StockPendingItem item = StockPendingItem.create(UUID.randomUUID(), 5, UUID.randomUUID());
        item.reduceQuantity(2);

        stockPendingUseCase.updatePendency(item);

        verify(repository, times(1)).save(item);
        assertThat(item.getQuantity()).isEqualTo(3);
    }
}
