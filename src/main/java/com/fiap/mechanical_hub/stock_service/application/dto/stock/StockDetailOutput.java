package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;

import java.util.List;
import java.util.UUID;

public record StockDetailOutput(UUID materialId, List<Stock> stocks, List<StockMovement> movements) {}
