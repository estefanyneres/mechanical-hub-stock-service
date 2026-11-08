package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import java.util.List;
import java.util.UUID;

public record ReserveMaterialsRequest(UUID serviceOrderId, List<MaterialQuantityRequest> items) {
}
