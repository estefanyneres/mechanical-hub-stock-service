package com.fiap.mechanical_hub.stock_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockMovementModel;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories.StockMovementJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementGatewayImplTest {

    @Mock
    private StockMovementJpaRepository jpaRepository;

    private StockMovementGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new StockMovementGatewayImpl(jpaRepository);
    }

    private StockMovementModel aModel() {
        return new StockMovementModel(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "RESERVADO", 5, LocalDateTime.now());
    }

    @Test
    @DisplayName("save deve persistir e retornar o domínio convertido")
    void save_ShouldPersistAndReturnDomain() {
        StockMovement movement = StockMovement.buildStockEntryMovement(UUID.randomUUID(), 3);
        StockMovementModel saved = aModel();
        when(jpaRepository.save(any(StockMovementModel.class))).thenReturn(saved);

        StockMovement result = gateway.save(movement);

        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("deleteById deve delegar pro repositório")
    void deleteById_ShouldDelegateToRepository() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    @DisplayName("findByMaterialId deve retornar lista convertida")
    void findByMaterialId_ShouldReturnConvertedList() {
        UUID materialId = UUID.randomUUID();
        when(jpaRepository.findByMaterialId(materialId)).thenReturn(List.of(aModel(), aModel()));

        List<StockMovement> result = gateway.findByMaterialId(materialId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("sumReservedQuantityByMaterialAndOrder deve delegar pro repositório")
    void sumReservedQuantityByMaterialAndOrder_ShouldDelegate() {
        UUID materialId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        when(jpaRepository.sumReservedQuantityByMaterialAndOrder(materialId, serviceOrderId)).thenReturn(12);

        Integer result = gateway.sumReservedQuantityByMaterialAndOrder(materialId, serviceOrderId);

        assertThat(result).isEqualTo(12);
    }

    @Test
    @DisplayName("findReservedMaterialIdsByServiceOrderId deve delegar pro repositório")
    void findReservedMaterialIdsByServiceOrderId_ShouldDelegate() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        when(jpaRepository.findReservedMaterialIdsByServiceOrderId(serviceOrderId)).thenReturn(List.of(materialId));

        List<UUID> result = gateway.findReservedMaterialIdsByServiceOrderId(serviceOrderId);

        assertThat(result).containsExactly(materialId);
    }

    @Test
    @DisplayName("deleteByMaterialId deve delegar pro repositório")
    void deleteByMaterialId_ShouldDelegateToRepository() {
        UUID materialId = UUID.randomUUID();

        gateway.deleteByMaterialId(materialId);

        verify(jpaRepository).deleteByMaterialId(materialId);
    }
}
