package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import java.util.UUID;

public record MaterialQuantityRequest(UUID materialId, Integer quantity) {
}
