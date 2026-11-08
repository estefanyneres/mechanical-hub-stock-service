package com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockJpaRepository extends JpaRepository<StockModel, UUID> {

    Optional<StockModel> findFirstByMaterialId(UUID materialId);

    Optional<StockModel> findByMaterialIdAndStatus(UUID materialId, StockStatusEnum status);

    List<StockModel> findAllByMaterialId(UUID materialId);

    void deleteByMaterialId(UUID materialId);
}
