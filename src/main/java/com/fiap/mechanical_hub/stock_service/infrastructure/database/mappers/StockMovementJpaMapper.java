package com.fiap.mechanical_hub.stock_service.infrastructure.database.mappers;

import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockMovementModel;

public class StockMovementJpaMapper {

    private StockMovementJpaMapper() {}

    public static StockMovementModel toJpa(StockMovement movement) {
        return new StockMovementModel(movement.getId(), movement.getMaterialId(), movement.getServiceOrderId(),
                movement.getMovementType(), movement.getQuantity(), movement.getCreatedAt());
    }

    public static StockMovement toDomain(StockMovementModel model) {
        return new StockMovement(model.getId(), model.getMaterialId(), model.getServiceOrderId(),
                model.getMovementType(), model.getQuantity(), model.getCreatedAt());
    }
}
