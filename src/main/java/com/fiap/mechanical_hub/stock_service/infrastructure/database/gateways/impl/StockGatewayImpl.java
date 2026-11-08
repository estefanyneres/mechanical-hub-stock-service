package com.fiap.mechanical_hub.stock_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.mappers.StockJpaMapper;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories.StockJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockGatewayImpl implements StockGateway {

    private final StockJpaRepository jpaRepository;

    @Override
    public Stock save(Stock stock) {
        var saved = jpaRepository.save(StockJpaMapper.toJpa(stock));
        return StockJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Stock> findByMaterialId(UUID materialId) {
        return jpaRepository.findFirstByMaterialId(materialId).map(StockJpaMapper::toDomain);
    }

    @Override
    public Optional<Stock> findById(UUID id) {
        return jpaRepository.findById(id).map(StockJpaMapper::toDomain);
    }

    @Override
    public Optional<Stock> findByMaterialIdAndStatus(UUID materialId, StockStatusEnum status) {
        return jpaRepository.findByMaterialIdAndStatus(materialId, status).map(StockJpaMapper::toDomain);
    }

    @Override
    public List<Stock> findAll() {
        return jpaRepository.findAll().stream().map(StockJpaMapper::toDomain).toList();
    }

    @Override
    public List<Stock> findAllByMaterialId(UUID materialId) {
        return jpaRepository.findAllByMaterialId(materialId).stream().map(StockJpaMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByMaterialId(UUID materialId) {
        jpaRepository.deleteByMaterialId(materialId);
    }

    @Override
    public void initializeForMaterial(UUID materialId) {
        save(Stock.setStockForNewMaterial(materialId));
    }
}
