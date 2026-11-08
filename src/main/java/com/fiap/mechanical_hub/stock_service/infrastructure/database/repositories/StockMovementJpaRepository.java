package com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.stock_service.domain.enums.StockMovementTypeEnum;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockMovementModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockMovementJpaRepository extends JpaRepository<StockMovementModel, UUID> {

    List<StockMovementModel> findByMaterialId(UUID materialId);

    void deleteByMaterialId(UUID materialId);

    @Query("""
            SELECT COALESCE(SUM(m.quantity), 0) FROM StockMovementModel m
            WHERE m.materialId = :materialId AND m.serviceOrderId = :serviceOrderId
            AND m.movementType = 'RESERVADO'
            """)
    Integer sumReservedQuantityByMaterialAndOrder(@Param("materialId") UUID materialId,
                                                   @Param("serviceOrderId") UUID serviceOrderId);

    @Query("""
            SELECT DISTINCT m.materialId FROM StockMovementModel m
            WHERE m.serviceOrderId = :serviceOrderId AND m.movementType = 'RESERVADO'
            """)
    List<UUID> findReservedMaterialIdsByServiceOrderId(@Param("serviceOrderId") UUID serviceOrderId);
}
