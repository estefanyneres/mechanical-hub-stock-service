package com.fiap.mechanical_hub.stock_service.application.dto.saga;

import java.util.UUID;

public record ExecucaoRevertidaEvent(UUID serviceOrderId) {
}
