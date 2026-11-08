package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import java.util.UUID;

public record StockSummaryResponse(
        UUID materialId,
        Integer totalQuantity,
        Integer availableQuantity,
        Integer reservedQuantity
) {}
