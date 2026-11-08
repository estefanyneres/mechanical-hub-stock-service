package com.fiap.mechanical_hub.stock_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockMovementGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.mappers.StockMovementJpaMapper;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories.StockMovementJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockMovementGatewayImpl implements StockMovementGateway {

    private final StockMovementJpaRepository jpaRepository;

    @Override
    public StockMovement save(StockMovement stockMovement) {
        var saved = jpaRepository.save(StockMovementJpaMapper.toJpa(stockMovement));
        return StockMovementJpaMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<StockMovement> findByMaterialId(UUID materialId) {
        return jpaRepository.findByMaterialId(materialId).stream().map(StockMovementJpaMapper::toDomain).toList();
    }

    @Override
    public Integer sumReservedQuantityByMaterialAndOrder(UUID materialId, UUID serviceOrderId) {
        return jpaRepository.sumReservedQuantityByMaterialAndOrder(materialId, serviceOrderId);
    }

    @Override
    public List<UUID> findReservedMaterialIdsByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findReservedMaterialIdsByServiceOrderId(serviceOrderId);
    }

    @Override
    public void deleteByMaterialId(UUID materialId) {
        jpaRepository.deleteByMaterialId(materialId);
    }
}
