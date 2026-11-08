package com.fiap.mechanical_hub.stock_service.application.gateways;

import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockGateway {

    Stock save(Stock stock);

    Optional<Stock> findByMaterialId(UUID materialId);

    Optional<Stock> findById(UUID id);

    Optional<Stock> findByMaterialIdAndStatus(UUID materialId, StockStatusEnum status);

    List<Stock> findAll();

    List<Stock> findAllByMaterialId(UUID materialId);

    void deleteById(UUID id);

    void deleteByMaterialId(UUID materialId);

    void initializeForMaterial(UUID materialId);
}
