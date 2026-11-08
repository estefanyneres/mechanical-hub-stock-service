package com.fiap.mechanical_hub.stock_service.domain.enums;

public enum StockMovementTypeEnum {
    ENTRY("ENTRADA"),
    RESERVED("RESERVADO"),
    RETURN("RETORNO"),
    EXCLUDED("EXCLUIDO"),
    OUT("SAÍDA");

    private final String description;

    StockMovementTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}
