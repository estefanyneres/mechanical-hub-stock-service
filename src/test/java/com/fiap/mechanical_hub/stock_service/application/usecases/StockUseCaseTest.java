package com.fiap.mechanical_hub.stock_service.application.usecases;

import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryResponse;
import com.fiap.mechanical_hub.stock_service.application.gateways.StockGateway;
import com.fiap.mechanical_hub.stock_service.application.gateways.StockMovementGateway;
import com.fiap.mechanical_hub.stock_service.application.presenters.StockPresenter;
import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;
import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import com.fiap.mechanical_hub.stock_service.domain.exceptions.BusinessRuleException;
import com.fiap.mechanical_hub.stock_service.domain.exceptions.NotFoundException;
import com.fiap.mechanical_hub.stock_service.infrastructure.messaging.SagaEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockUseCaseTest {

    @Mock
    private StockMovementUseCase stockMovementUseCase;
    @Mock
    private StockPendingUseCase stockPendingUseCase;
    @Mock
    private SagaEventPublisher sagaEventPublisher;
    @Mock
    private StockMovementGateway stockMovementGateway;
    @Mock
    private StockGateway stockGateway;
    @Mock
    private StockPresenter stockPresenter;

    @InjectMocks
    private StockUseCase stockUseCase;

    @Test
    @DisplayName("Deve inicializar estoque para novo material")
    void shouldSetStockForNewMaterial() {
        UUID materialId = UUID.randomUUID();
        stockUseCase.setStockForNewMaterial(materialId);
        verify(stockGateway, times(1)).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar detalhe de material inexistente")
    void shouldThrowExceptionWhenMaterialNotFoundInStock() {
        UUID materialId = UUID.randomUUID();
        when(stockGateway.findAllByMaterialId(materialId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> stockUseCase.findByMaterialId(materialId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Deve registrar entrada de estoque e resolver pendências")
    void shouldRegisterStockEntryAndResolveIssues() {
        UUID materialId = UUID.randomUUID();
        Stock existingStock = new Stock(UUID.randomUUID(), materialId, 10, StockStatusEnum.AVAILABLE, null);

        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.of(existingStock));
        when(stockGateway.save(any(Stock.class))).thenAnswer(i -> i.getArgument(0));
        when(stockPendingUseCase.findMaterialStockPendency(materialId)).thenReturn(Collections.emptyList());

        stockUseCase.registerStockEntry(materialId, 50);

        assertThat(existingStock.getQuantity()).isEqualTo(60);
        verify(stockMovementUseCase).registerStockEntryMovement(materialId, 50);
        verify(stockPendingUseCase).findMaterialStockPendency(materialId);
    }

    @Test
    @DisplayName("Deve lançar NotFoundException ao registrar entrada sem estoque disponível")
    void registerStockEntry_ShouldThrowNotFoundException_WhenStockRecordDoesNotExist() {
        UUID materialId = UUID.randomUUID();
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockUseCase.registerStockEntry(materialId, 10))
                .isInstanceOf(NotFoundException.class);

        verify(stockGateway, never()).save(any(Stock.class));
        verify(stockMovementUseCase, never()).registerStockEntryMovement(any(), any());
    }

    @Test
    @DisplayName("Deve criar pendência quando não houver estoque disponível para reserva")
    void shouldCreatePendencyWhenStockIsInsufficient() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        Stock lowStock = new Stock(UUID.randomUUID(), materialId, 2, StockStatusEnum.AVAILABLE, null);

        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.of(lowStock));

        boolean hasPendency = stockUseCase.reserveForServiceOrder(serviceOrderId, materialId, 10);

        assertThat(hasPendency).isTrue();
        verify(stockPendingUseCase).createStockPendency(serviceOrderId, materialId, 10);
    }

    @Test
    @DisplayName("Deve reservar com sucesso quando houver estoque disponível")
    void shouldReserveSuccessfully_WhenStockIsSufficient() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        Stock availableStock = new Stock(UUID.randomUUID(), materialId, 10, StockStatusEnum.AVAILABLE, null);

        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.of(availableStock));
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED))
                .thenReturn(Optional.empty());

        boolean hasPendency = stockUseCase.reserveForServiceOrder(serviceOrderId, materialId, 6);

        assertThat(hasPendency).isFalse();
        verify(stockPendingUseCase, never()).createStockPendency(any(), any(), any());
        verify(stockMovementUseCase).registerReservationMovement(materialId, serviceOrderId, 6);
        assertThat(availableStock.getQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deve atualizar estoque reservado existente quando já houver registro")
    void executeReservation_ShouldUpdateExistingReservedStock() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        Stock availableStock = mock(Stock.class);
        when(availableStock.checkMaterialAvailability(5)).thenReturn(false);

        Stock existingReservedStock = mock(Stock.class);

        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.of(availableStock));
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED))
                .thenReturn(Optional.of(existingReservedStock));

        stockUseCase.reserveForServiceOrder(serviceOrderId, materialId, 5);

        verify(existingReservedStock).addQuantity(5);
        verify(stockGateway).save(existingReservedStock);
        verify(stockGateway).save(availableStock);
    }

    @Test
    @DisplayName("Deve publicar estoque.reabastecido quando a última pendência da OS é resolvida")
    void resolveMaterialPendingIssues_ShouldPublishEstoqueReabastecido_WhenNoOtherPendencies() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Stock updatedStock = mock(Stock.class);
        when(updatedStock.getQuantity()).thenReturn(10);

        StockPendingItem pending = mock(StockPendingItem.class);
        when(pending.getQuantity()).thenReturn(5);
        when(pending.getServiceOrderId()).thenReturn(orderId);
        when(pending.getMaterialId()).thenReturn(materialId);

        when(stockPendingUseCase.findMaterialStockPendency(materialId)).thenReturn(List.of(pending));
        when(stockPendingUseCase.findOrderStockPendency(orderId)).thenReturn(List.of());
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)).thenReturn(Optional.empty());

        stockUseCase.resolveMaterialPendingIssues(materialId, updatedStock);

        verify(stockPendingUseCase).removePendency(pending);
        verify(updatedStock).subtractQuantity(5);
        verify(sagaEventPublisher).publishEstoqueReabastecido(orderId);
        verify(stockGateway, atLeastOnce()).save(any(Stock.class));
    }

    @Test
    @DisplayName("Não deve publicar estoque.reabastecido quando ainda houver pendência de outro material")
    void resolveMaterialPendingIssues_ShouldNotPublish_WhenOtherMaterialStillPending() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID pendingId = UUID.randomUUID();

        Stock updatedStock = mock(Stock.class);
        when(updatedStock.getQuantity()).thenReturn(5);

        StockPendingItem pending = mock(StockPendingItem.class);
        when(pending.getId()).thenReturn(pendingId);
        when(pending.getQuantity()).thenReturn(5);
        when(pending.getServiceOrderId()).thenReturn(orderId);
        when(pending.getMaterialId()).thenReturn(materialId);

        StockPendingItem otherMaterialPending = mock(StockPendingItem.class);
        when(otherMaterialPending.getId()).thenReturn(UUID.randomUUID());

        when(stockPendingUseCase.findMaterialStockPendency(materialId)).thenReturn(List.of(pending));
        when(stockPendingUseCase.findOrderStockPendency(orderId)).thenReturn(List.of(otherMaterialPending));
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)).thenReturn(Optional.empty());

        stockUseCase.resolveMaterialPendingIssues(materialId, updatedStock);

        verify(stockPendingUseCase).removePendency(pending);
        verify(sagaEventPublisher, never()).publishEstoqueReabastecido(any());
    }

    @Test
    @DisplayName("Deve interromper a resolução de pendências quando o estoque disponível chegar a zero")
    void resolveMaterialPendingIssues_ShouldBreakLoopWhenStockExhausted() {
        UUID materialId = UUID.randomUUID();

        Stock updatedStock = mock(Stock.class);
        when(updatedStock.getQuantity()).thenReturn(2, 0);

        StockPendingItem firstPending = mock(StockPendingItem.class);
        when(firstPending.getQuantity()).thenReturn(5);
        when(firstPending.getMaterialId()).thenReturn(materialId);
        when(firstPending.getServiceOrderId()).thenReturn(UUID.randomUUID());

        StockPendingItem secondPending = mock(StockPendingItem.class);

        when(stockPendingUseCase.findMaterialStockPendency(materialId))
                .thenReturn(List.of(firstPending, secondPending));
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED))
                .thenReturn(Optional.empty());

        stockUseCase.resolveMaterialPendingIssues(materialId, updatedStock);

        verify(updatedStock, times(1)).subtractQuantity(anyInt());
        verify(stockPendingUseCase, times(1)).updatePendency(firstPending);
        verify(stockPendingUseCase, never()).removePendency(secondPending);
    }

    @Test
    @DisplayName("Deve restaurar itens reservados usando o histórico de movimentos, sem depender de tarefas")
    void shouldRestoreReservedItemsUsingMovementHistory() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Stock reservedStock = new Stock(UUID.randomUUID(), materialId, 5, StockStatusEnum.RESERVED, null);
        Stock availableStock = new Stock(UUID.randomUUID(), materialId, 0, StockStatusEnum.AVAILABLE, null);

        when(stockMovementGateway.findReservedMaterialIdsByServiceOrderId(orderId)).thenReturn(List.of(materialId));
        when(stockMovementGateway.sumReservedQuantityByMaterialAndOrder(materialId, orderId)).thenReturn(5);
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)).thenReturn(Optional.of(reservedStock));
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE)).thenReturn(Optional.of(availableStock));

        stockUseCase.restoreReservedItems(orderId);

        assertThat(reservedStock.getQuantity()).isZero();
        assertThat(availableStock.getQuantity()).isEqualTo(5);
        verify(stockMovementUseCase).registerStockReturnMovement(materialId, orderId, 5);
        verify(stockPendingUseCase).clearOrderPendencies(orderId);
    }

    @Test
    @DisplayName("Deve pular material sem estoque reservado e ainda assim limpar pendências")
    void shouldSkipMaterialWithNoReservedStockAndClearPendencies() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(stockMovementGateway.findReservedMaterialIdsByServiceOrderId(orderId)).thenReturn(List.of(materialId));
        when(stockMovementGateway.sumReservedQuantityByMaterialAndOrder(materialId, orderId)).thenReturn(5);
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)).thenReturn(Optional.empty());

        stockUseCase.restoreReservedItems(orderId);

        verify(stockGateway, never()).findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE);
        verify(stockMovementUseCase, never()).registerStockReturnMovement(any(), any(), any());
        verify(stockPendingUseCase).clearOrderPendencies(orderId);
    }

    @Test
    @DisplayName("Deve limpar pendências mesmo sem nenhum material reservado pra essa OS")
    void shouldClearPendenciesEvenWithNoReservedMaterials() {
        UUID orderId = UUID.randomUUID();
        when(stockMovementGateway.findReservedMaterialIdsByServiceOrderId(orderId)).thenReturn(List.of());

        stockUseCase.restoreReservedItems(orderId);

        verify(stockPendingUseCase).clearOrderPendencies(orderId);
        verifyNoInteractions(stockGateway);
    }

    @Test
    @DisplayName("Deve pular material cuja soma reservada para a OS seja zero ou negativa")
    void shouldSkipMaterialWithNonPositiveReservedSum() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(stockMovementGateway.findReservedMaterialIdsByServiceOrderId(orderId)).thenReturn(List.of(materialId));
        when(stockMovementGateway.sumReservedQuantityByMaterialAndOrder(materialId, orderId)).thenReturn(0);

        stockUseCase.restoreReservedItems(orderId);

        verify(stockGateway, never()).findByMaterialIdAndStatus(any(), any());
        verify(stockMovementUseCase, never()).registerStockReturnMovement(any(), any(), any());
        verify(stockPendingUseCase).clearOrderPendencies(orderId);
    }

    @Test
    @DisplayName("Deve retornar todos os resumos de estoque")
    void findAll_Success() {
        UUID materialId = UUID.randomUUID();
        List<Stock> stocks = List.of(new Stock(UUID.randomUUID(), materialId, 10, StockStatusEnum.AVAILABLE, null));
        StockSummaryResponse summaryResponse = new StockSummaryResponse(materialId, 10, 10, 0);

        when(stockGateway.findAll()).thenReturn(stocks);
        when(stockPresenter.toSummaryList(any(StockSummaryOutput.class))).thenReturn(List.of(summaryResponse));

        List<StockSummaryResponse> result = stockUseCase.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).materialId()).isEqualTo(materialId);
        verify(stockGateway).findAll();
    }

    @Test
    @DisplayName("Deve retornar detalhes do estoque")
    void findByMaterialId_Success() {
        UUID materialId = UUID.randomUUID();
        Stock available = mock(Stock.class);
        StockDetailResponse expectedResponse = new StockDetailResponse(materialId, 10, 8, 2, List.of());

        when(stockGateway.findAllByMaterialId(materialId)).thenReturn(List.of(available));
        when(stockMovementGateway.findByMaterialId(materialId)).thenReturn(Collections.emptyList());
        when(stockPresenter.toDetailResponse(any(StockDetailOutput.class))).thenReturn(expectedResponse);

        StockDetailResponse result = stockUseCase.findByMaterialId(materialId);

        assertThat(result.materialId()).isEqualTo(materialId);
        verify(stockPresenter).toDetailResponse(any(StockDetailOutput.class));
    }

    @Test
    @DisplayName("Deve deletar estoque do material com sucesso")
    void delete_Success() {
        UUID materialId = UUID.randomUUID();
        Stock availableStock = mock(Stock.class);
        when(availableStock.getQuantity()).thenReturn(100);
        when(availableStock.getStatus()).thenReturn(StockStatusEnum.AVAILABLE);

        when(stockGateway.findAllByMaterialId(materialId)).thenReturn(List.of(availableStock));
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.of(availableStock));

        stockUseCase.deleteStockForMaterial(materialId);

        verify(stockMovementUseCase).registerStockDeleteMovement(materialId, 100);
        verify(stockGateway).deleteByMaterialId(materialId);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando material possui estoque reservado")
    void delete_ShouldThrowBusinessRuleException_WhenStockIsReserved() {
        UUID materialId = UUID.randomUUID();
        Stock reservedStock = mock(Stock.class);
        when(reservedStock.getStatus()).thenReturn(StockStatusEnum.RESERVED);
        when(stockGateway.findAllByMaterialId(materialId)).thenReturn(List.of(reservedStock));

        assertThatThrownBy(() -> stockUseCase.deleteStockForMaterial(materialId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Não é possível excluir material com estoque reservado.");

        verify(stockGateway, never()).deleteByMaterialId(any());
    }

    @Test
    @DisplayName("Deve registrar saída de estoque com sucesso")
    void registerStockOut_Success() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        Stock reservedStock = mock(Stock.class);
        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED))
                .thenReturn(Optional.of(reservedStock));

        stockUseCase.registerStockOut(orderId, materialId, 5);

        verify(reservedStock).decreaseReserved(5);
        verify(stockGateway).save(reservedStock);
        verify(stockMovementUseCase).registerStockOutMovement(materialId, orderId, 5);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando não houver estoque reservado pra dar saída")
    void registerStockOut_ReservedStockNotFound() {
        UUID materialId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockUseCase.registerStockOut(orderId, materialId, 5))
                .isInstanceOf(BusinessRuleException.class);

        verify(stockMovementUseCase, never()).registerStockOutMovement(any(), any(), any());
    }
}
