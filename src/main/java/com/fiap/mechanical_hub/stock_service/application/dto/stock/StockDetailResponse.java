package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import java.util.List;
import java.util.UUID;

public record StockDetailResponse(
        UUID materialId,
        Integer totalQuantity,
        Integer availableQuantity,
        Integer reservedQuantity,
        List<StockMovementResponse> movements
) {}
