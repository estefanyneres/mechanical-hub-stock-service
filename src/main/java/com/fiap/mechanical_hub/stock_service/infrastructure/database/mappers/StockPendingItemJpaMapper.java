package com.fiap.mechanical_hub.stock_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockPendingItemModel;

public class StockPendingItemJpaMapper {

    private StockPendingItemJpaMapper() {}

    public static StockPendingItemModel toJpa(StockPendingItem item) {
        return new StockPendingItemModel(item.getId(), item.getServiceOrderId(), item.getMaterialId(),
                item.getQuantity(), item.getCreatedAt());
    }

    public static StockPendingItem toDomain(StockPendingItemModel model) {
        return new StockPendingItem(model.getId(), model.getServiceOrderId(), model.getMaterialId(),
                model.getQuantity(), model.getCreatedAt());
    }
}
