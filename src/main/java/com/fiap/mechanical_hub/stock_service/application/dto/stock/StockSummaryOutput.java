package com.fiap.mechanical_hub.stock_service.application.dto.stock;

import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;

import java.util.List;

public record StockSummaryOutput(List<Stock> stocks) {}
