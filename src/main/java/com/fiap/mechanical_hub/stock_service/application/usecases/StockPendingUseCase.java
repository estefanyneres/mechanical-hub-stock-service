package com.fiap.mechanical_hub.stock_service.application.usecases;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockPendingItemGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Sem NotificationUseCase (email/WhatsApp) -- essa integracao do monolito
 * nao e exigida por nenhuma task da Fase 4, so loga.
 */
@Slf4j
public class StockPendingUseCase {

    private final StockPendingItemGateway stockPendingItemGateway;

    public StockPendingUseCase(StockPendingItemGateway stockPendingItemGateway) {
        this.stockPendingItemGateway = stockPendingItemGateway;
    }

    public void createStockPendency(UUID serviceOrderId, UUID materialId, Integer quantity) {
        StockPendingItem pendingItem = StockPendingItem.create(serviceOrderId, quantity, materialId);
        stockPendingItemGateway.save(pendingItem);
        log.warn("Pendência de estoque criada: material {} indisponível pra OS {} (faltam {})",
                materialId, serviceOrderId, quantity);
    }

    public List<StockPendingItem> findMaterialStockPendency(UUID materialId) {
        return stockPendingItemGateway.findByMaterialIdOrderByCreatedAtAsc(materialId);
    }

    public List<StockPendingItem> findOrderStockPendency(UUID serviceOrderId) {
        return stockPendingItemGateway.findByServiceOrderId(serviceOrderId);
    }

    public void updatePendency(StockPendingItem pendency) {
        stockPendingItemGateway.save(pendency);
    }

    public void clearOrderPendencies(UUID serviceOrderId) {
        stockPendingItemGateway.deleteByServiceOrderId(serviceOrderId);
    }

    public void removePendency(StockPendingItem pendency) {
        stockPendingItemGateway.delete(pendency);
    }
}
