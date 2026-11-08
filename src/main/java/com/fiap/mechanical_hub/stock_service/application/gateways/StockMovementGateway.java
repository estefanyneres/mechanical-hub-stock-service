package com.fiap.mechanical_hub.stock_service.application.gateways;

import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;

import java.util.List;
import java.util.UUID;

public interface StockMovementGateway {

    StockMovement save(StockMovement stockMovement);

    void deleteById(UUID id);

    List<StockMovement> findByMaterialId(UUID materialId);

    Integer sumReservedQuantityByMaterialAndOrder(UUID materialId, UUID serviceOrderId);

    /**
     * Materiais distintos com movimento RESERVADO pra essa OS -- usado por
     * restoreReservedItems (TASK-028) pra saber o que liberar, sem precisar
     * da lista de tarefas do repair-service (esse servico nao tem acesso
     * a isso, so ao proprio historico de movimentos).
     */
    List<UUID> findReservedMaterialIdsByServiceOrderId(UUID serviceOrderId);

    void deleteByMaterialId(UUID materialId);
}
