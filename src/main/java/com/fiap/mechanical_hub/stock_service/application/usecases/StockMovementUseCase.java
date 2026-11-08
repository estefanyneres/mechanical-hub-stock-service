package com.fiap.mechanical_hub.stock_service.application.usecases;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockMovementGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;

import java.util.UUID;

public class StockMovementUseCase {

    private final StockMovementGateway stockMovementGateway;

    public StockMovementUseCase(StockMovementGateway stockMovementGateway) {
        this.stockMovementGateway = stockMovementGateway;
    }

    public void registerStockEntryMovement(UUID materialId, Integer quantity) {
        StockMovement movement = StockMovement.buildStockEntryMovement(materialId, quantity);
        stockMovementGateway.save(movement);
    }

    public void registerReservationMovement(UUID materialId, UUID serviceOrderId, Integer quantity) {
        StockMovement movement = StockMovement.registerReservation(materialId, serviceOrderId, quantity);
        stockMovementGateway.save(movement);
    }

    public void registerStockReturnMovement(UUID materialId, UUID serviceOrderId, Integer quantity) {
        StockMovement movement = StockMovement.registerReturn(materialId, serviceOrderId, quantity);
        stockMovementGateway.save(movement);
    }

    public void registerStockDeleteMovement(UUID materialId, Integer quantity) {
        StockMovement movement = StockMovement.registerDelete(materialId, quantity);
        stockMovementGateway.save(movement);
    }

    public void registerStockOutMovement(UUID materialId, UUID serviceOrderId, Integer quantity) {
        StockMovement movement = StockMovement.registerStockOut(materialId, serviceOrderId, quantity);
        stockMovementGateway.save(movement);
    }
}
