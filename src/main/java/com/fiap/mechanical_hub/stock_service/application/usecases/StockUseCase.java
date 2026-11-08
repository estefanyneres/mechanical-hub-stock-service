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
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Adaptado pra Fase 4 (TASK-028):
 * - reserveForServiceOrder/registerStockOut recebem so os IDs e a
 *   quantidade, nao objetos ServiceOrder/Material/OrderTask (esse servico
 *   nao tem acesso a essas entidades - ADR-001, cada servico so conhece o
 *   proprio banco).
 * - resolveMaterialPendingIssues: quando uma OS fica sem nenhuma pendencia,
 *   publica "estoque.reabastecido" em vez de escrever hasStockPending
 *   direto numa ServiceOrder (que nao existe aqui).
 * - restoreReservedItems: recebe so o serviceOrderId e usa o PROPRIO
 *   historico de StockMovement pra saber o que tinha sido reservado --
 *   nao itera OrderTask (dado do repair-service, que esse servico nao
 *   tem). Decisao consciente de liberar TUDO que foi reservado pra essa
 *   OS, nao so a diferenca entre diagnostico antigo/novo (RFC-001).
 * - Sem alerta de estoque minimo: dependia de Material.minStockQuantity,
 *   que pertence ao catalogo do repair-service (ADR-001) - nao portado
 *   nesta fase, exigiria uma consulta cross-service que ainda nao existe.
 */
@Slf4j
public class StockUseCase {

    private final StockMovementUseCase stockMovementUseCase;
    private final StockPendingUseCase stockPendingUseCase;
    private final SagaEventPublisher sagaEventPublisher;

    private final StockMovementGateway stockMovementGateway;
    private final StockGateway stockGateway;
    private final StockPresenter stockPresenter;

    public StockUseCase(StockMovementUseCase stockMovementUseCase, StockPendingUseCase stockPendingUseCase,
                        SagaEventPublisher sagaEventPublisher, StockMovementGateway stockMovementGateway,
                        StockGateway stockGateway, StockPresenter stockPresenter) {
        this.stockMovementUseCase = stockMovementUseCase;
        this.stockPendingUseCase = stockPendingUseCase;
        this.sagaEventPublisher = sagaEventPublisher;
        this.stockMovementGateway = stockMovementGateway;
        this.stockGateway = stockGateway;
        this.stockPresenter = stockPresenter;
    }

    public void setStockForNewMaterial(UUID materialId) {
        Stock stock = Stock.setStockForNewMaterial(materialId);
        stockGateway.save(stock);
    }

    public List<StockSummaryResponse> findAll() {
        List<Stock> allStocks = stockGateway.findAll();
        return stockPresenter.toSummaryList(new StockSummaryOutput(allStocks));
    }

    public StockDetailResponse findByMaterialId(UUID materialId) {
        List<Stock> stocks = stockGateway.findAllByMaterialId(materialId);

        if (stocks.isEmpty()) {
            throw new NotFoundException("Estoque não encontrado para o material id: " + materialId);
        }

        var movements = stockMovementGateway.findByMaterialId(materialId);
        StockDetailOutput output = new StockDetailOutput(materialId, stocks, movements);
        return stockPresenter.toDetailResponse(output);
    }

    public void registerStockEntry(UUID materialId, Integer quantity) {
        Stock updatedStock = registerMaterialEntry(materialId, quantity);
        resolveMaterialPendingIssues(materialId, updatedStock);
    }

    public void resolveMaterialPendingIssues(UUID materialId, Stock updatedStock) {
        List<StockPendingItem> pendingIssues = stockPendingUseCase.findMaterialStockPendency(materialId);

        for (StockPendingItem pending : pendingIssues) {
            int available = updatedStock.getQuantity();

            if (available <= 0) {
                break;
            }

            int pendingQuantity = pending.getQuantity();
            int reservedQuantity = Math.min(available, pendingQuantity);

            executeReservation(pending.getServiceOrderId(), pending.getMaterialId(), reservedQuantity, updatedStock);

            if (reservedQuantity == pendingQuantity) {
                stockPendingUseCase.removePendency(pending);
                boolean hasOtherPendencies = stockPendingUseCase.findOrderStockPendency(pending.getServiceOrderId())
                        .stream()
                        .anyMatch(p -> !p.getId().equals(pending.getId()));
                if (!hasOtherPendencies) {
                    sagaEventPublisher.publishEstoqueReabastecido(pending.getServiceOrderId());
                    log.info("Todas as pendências da OS {} resolvidas - estoque.reabastecido publicado",
                            pending.getServiceOrderId());
                }
            } else {
                pending.reduceQuantity(reservedQuantity);
                stockPendingUseCase.updatePendency(pending);
            }
        }
    }

    private Stock registerMaterialEntry(UUID materialId, Integer quantity) {
        Stock stock = stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE)
                .orElseThrow(() -> new NotFoundException("Estoque não encontrado para o material id: " + materialId));

        stock.addQuantity(quantity);
        Stock updatedStock = stockGateway.save(stock);
        stockMovementUseCase.registerStockEntryMovement(materialId, quantity);
        return updatedStock;
    }

    private void executeReservation(UUID serviceOrderId, UUID materialId, Integer quantity, Stock availableStock) {
        availableStock.subtractQuantity(quantity);
        stockGateway.save(availableStock);

        Stock reservedStock = stockGateway
                .findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)
                .orElse(null);

        if (reservedStock == null) {
            Stock newReservedStock = Stock.createReservedStock(materialId, quantity);
            stockGateway.save(newReservedStock);
        } else {
            reservedStock.addQuantity(quantity);
            stockGateway.save(reservedStock);
        }

        stockMovementUseCase.registerReservationMovement(materialId, serviceOrderId, quantity);
    }

    /**
     * @return true se ficou pendente (sem estoque suficiente), false se a
     * reserva foi feita com sucesso -- mesma semantica do metodo original
     * no monolito.
     */
    public boolean reserveForServiceOrder(UUID serviceOrderId, UUID materialId, Integer quantity) {
        Stock availableStock = stockGateway
                .findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE)
                .orElse(null);

        if (availableStock == null || availableStock.checkMaterialAvailability(quantity)) {
            stockPendingUseCase.createStockPendency(serviceOrderId, materialId, quantity);
            return true;
        }

        executeReservation(serviceOrderId, materialId, quantity, availableStock);
        return false;
    }

    /**
     * Libera TODO material reservado pra essa OS (nao so a diferenca entre
     * diagnostico antigo/novo - RFC-001), usando o proprio historico de
     * movimentos pra saber quais materiais tem reserva, ja que este
     * servico nao tem acesso a lista de tarefas do repair-service.
     */
    public void restoreReservedItems(UUID serviceOrderId) {
        List<UUID> reservedMaterialIds = stockMovementGateway.findReservedMaterialIdsByServiceOrderId(serviceOrderId);

        for (UUID materialId : reservedMaterialIds) {
            int reservedForThisOrder = stockMovementGateway.sumReservedQuantityByMaterialAndOrder(materialId, serviceOrderId);
            if (reservedForThisOrder <= 0) {
                continue;
            }

            Stock reservedStock = stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)
                    .orElse(null);
            if (reservedStock == null) {
                continue;
            }

            int quantityToRelease = Math.min(reservedStock.getQuantity(), reservedForThisOrder);
            reservedStock.release(quantityToRelease);
            stockGateway.save(reservedStock);

            Stock availableStock = stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE)
                    .orElseThrow(() -> new NotFoundException("Estoque disponível não encontrado para o material: " + materialId));

            availableStock.replenish(quantityToRelease);
            stockGateway.save(availableStock);

            stockMovementUseCase.registerStockReturnMovement(materialId, serviceOrderId, quantityToRelease);
        }

        stockPendingUseCase.clearOrderPendencies(serviceOrderId);
    }

    public void deleteStockForMaterial(UUID materialId) {
        List<Stock> materialStock = stockGateway.findAllByMaterialId(materialId);
        if (materialStock.stream().anyMatch(s -> s.getStatus() == StockStatusEnum.RESERVED)) {
            throw new BusinessRuleException("Não é possível excluir material com estoque reservado.");
        }

        Integer availableQuantity = stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.AVAILABLE)
                .orElseThrow(() -> new NotFoundException("Estoque disponível não encontrado para o material: " + materialId))
                .getQuantity();
        stockMovementUseCase.registerStockDeleteMovement(materialId, availableQuantity);
        stockGateway.deleteByMaterialId(materialId);
    }

    public void registerStockOut(UUID serviceOrderId, UUID materialId, Integer quantity) {
        Stock stock = stockGateway.findByMaterialIdAndStatus(materialId, StockStatusEnum.RESERVED)
                .orElseThrow(() -> new BusinessRuleException(
                        "Não é possível dar saída: o material '" + materialId + "' não está reservado"));

        stock.decreaseReserved(quantity);
        stockGateway.save(stock);

        stockMovementUseCase.registerStockOutMovement(materialId, serviceOrderId, quantity);
    }
}
