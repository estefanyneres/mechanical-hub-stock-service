package com.fiap.mechanical_hub.stock_service.domain.entities;

import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import com.fiap.mechanical_hub.stock_service.domain.exceptions.BusinessRuleException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Stock {

    private UUID id;
    private UUID materialId;
    private Integer quantity;
    private StockStatusEnum status;
    private LocalDateTime updatedAt;

    public Stock() {
    }

    public Stock(UUID id, UUID materialId, Integer quantity, StockStatusEnum status, LocalDateTime updatedAt) {
        this.id = id;
        this.materialId = materialId;
        this.quantity = quantity;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static Stock createReservedStock(UUID materialId, Integer quantity) {
        Stock stock = new Stock();
        stock.id = UUID.randomUUID();
        stock.materialId = materialId;
        stock.quantity = quantity;
        stock.status = StockStatusEnum.RESERVED;
        stock.updatedAt = LocalDateTime.now();

        return stock;
    }

    public static Stock setStockForNewMaterial(UUID materialId) {
        Stock stock = new Stock();
        stock.id = UUID.randomUUID();
        stock.materialId = materialId;
        stock.quantity = 0;
        stock.status = StockStatusEnum.AVAILABLE;
        stock.updatedAt = LocalDateTime.now();

        return stock;
    }

    public void addQuantity(Integer quantity) {
        if (quantity < 0) { throw new IllegalArgumentException("Quantidade não pode ser negativa"); }
        this.quantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void subtractQuantity(Integer quantity) {
        if (quantity > this.quantity) { throw new IllegalArgumentException("Quantidade insuficiente"); }
        this.quantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean checkMaterialAvailability(Integer quantity) {
        return this.status.equals(StockStatusEnum.AVAILABLE) && this.quantity < quantity;
    }

    public UUID getId() { return id; }
    public UUID getMaterialId() { return materialId; }
    public Integer getQuantity() { return quantity; }
    public StockStatusEnum getStatus() { return status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void release(int quantity) {
        if (this.quantity < quantity) {
            throw new BusinessRuleException("Quantidade reservada insuficiente para liberar o material: " + this.materialId);
        }
        this.quantity -= quantity;
    }

    public void replenish(int quantity) {
        this.quantity += quantity;
    }

    public void decreaseReserved(int quantity) {
        if (this.status != StockStatusEnum.RESERVED) {
            throw new BusinessRuleException("Não é possível dar saída em um item que não está reservado");
        }
        if (this.quantity < quantity) {
            throw new BusinessRuleException("Quantidade reservada insuficiente para dar saída");
        }

        this.quantity -= quantity;

        this.updatedAt = LocalDateTime.now();
    }

}
