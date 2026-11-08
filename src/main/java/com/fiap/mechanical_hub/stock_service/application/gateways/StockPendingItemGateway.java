package com.fiap.mechanical_hub.stock_service.application.gateways;

import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockPendingItemGateway {

    StockPendingItem save(StockPendingItem stockPendingItem);

    Optional<StockPendingItem> findById(UUID id);

    List<StockPendingItem> findByMaterialIdOrderByCreatedAtAsc(UUID materialId);

    List<StockPendingItem> findByServiceOrderId(UUID serviceOrderId);

    void deleteById(UUID id);

    void delete(StockPendingItem stockPendingItem);

    void deleteByServiceOrderId(UUID serviceOrderId);
}
