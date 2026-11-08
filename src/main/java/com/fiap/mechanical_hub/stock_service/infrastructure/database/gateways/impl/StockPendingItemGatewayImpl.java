package com.fiap.mechanical_hub.stock_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockPendingItemGateway;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.mappers.StockPendingItemJpaMapper;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories.StockPendingItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockPendingItemGatewayImpl implements StockPendingItemGateway {

    private final StockPendingItemJpaRepository jpaRepository;

    @Override
    public StockPendingItem save(StockPendingItem stockPendingItem) {
        var saved = jpaRepository.save(StockPendingItemJpaMapper.toJpa(stockPendingItem));
        return StockPendingItemJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<StockPendingItem> findById(UUID id) {
        return jpaRepository.findById(id).map(StockPendingItemJpaMapper::toDomain);
    }

    @Override
    public List<StockPendingItem> findByMaterialIdOrderByCreatedAtAsc(UUID materialId) {
        return jpaRepository.findByMaterialIdOrderByCreatedAtAsc(materialId).stream()
                .map(StockPendingItemJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<StockPendingItem> findByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findByServiceOrderId(serviceOrderId).stream()
                .map(StockPendingItemJpaMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(StockPendingItem stockPendingItem) {
        jpaRepository.deleteById(stockPendingItem.getId());
    }

    @Override
    public void deleteByServiceOrderId(UUID serviceOrderId) {
        jpaRepository.deleteByServiceOrderId(serviceOrderId);
    }
}
