package com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockPendingItemModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockPendingItemJpaRepository extends JpaRepository<StockPendingItemModel, UUID> {

    List<StockPendingItemModel> findByMaterialIdOrderByCreatedAtAsc(UUID materialId);

    List<StockPendingItemModel> findByServiceOrderId(UUID serviceOrderId);

    void deleteByServiceOrderId(UUID serviceOrderId);
}
