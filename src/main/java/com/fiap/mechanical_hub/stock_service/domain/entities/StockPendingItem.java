package com.fiap.mechanical_hub.stock_service.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class StockPendingItem {

    private UUID id;
    private UUID serviceOrderId;
    private UUID materialId;
    private Integer quantity;
    private LocalDateTime createdAt;

    public StockPendingItem() {}

    public StockPendingItem(UUID id, UUID serviceOrderId, UUID materialId, Integer quantity, LocalDateTime createdAt) {
        this.id = id;
        this.serviceOrderId = serviceOrderId;
        this.materialId = materialId;
        this.quantity = quantity;
        this.createdAt = createdAt;
    }

    public void reduceQuantity(int amount) {
        this.quantity -= amount;
    }

    public static StockPendingItem create(UUID serviceOrderId, Integer quantity, UUID materialId) {
        return new StockPendingItem(
                UUID.randomUUID(),
                serviceOrderId,
                materialId,
                quantity,
                LocalDateTime.now()
        );
    }

    public UUID getId() { return id; }
    public UUID getServiceOrderId() { return serviceOrderId; }
    public UUID getMaterialId() { return materialId; }
    public Integer getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
