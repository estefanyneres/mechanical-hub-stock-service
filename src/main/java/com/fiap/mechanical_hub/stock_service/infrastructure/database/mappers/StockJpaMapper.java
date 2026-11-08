package com.fiap.mechanical_hub.stock_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockModel;

public class StockJpaMapper {

    private StockJpaMapper() {}

    public static StockModel toJpa(Stock stock) {
        return new StockModel(stock.getId(), stock.getMaterialId(), stock.getQuantity(),
                stock.getStatus(), stock.getUpdatedAt());
    }

    public static Stock toDomain(StockModel model) {
        return new Stock(model.getId(), model.getMaterialId(), model.getQuantity(),
                model.getStatus(), model.getUpdatedAt());
    }
}
