package com.fiap.mechanical_hub.stock_service.domain.enums;

public enum StockStatusEnum {
    AVAILABLE("Disponível"),
    RESERVED("Reservado");

    private final String description;

    StockStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
