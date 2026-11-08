package com.fiap.mechanical_hub.stock_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.stock_service.domain.entities.StockPendingItem;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockPendingItemModel;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories.StockPendingItemJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockPendingItemGatewayImplTest {

    @Mock
    private StockPendingItemJpaRepository jpaRepository;

    private StockPendingItemGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new StockPendingItemGatewayImpl(jpaRepository);
    }

    private StockPendingItemModel aModel() {
        return new StockPendingItemModel(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                5, LocalDateTime.now());
    }

    @Test
    @DisplayName("save deve persistir e retornar o domínio convertido")
    void save_ShouldPersistAndReturnDomain() {
        StockPendingItem item = StockPendingItem.create(UUID.randomUUID(), 5, UUID.randomUUID());
        StockPendingItemModel saved = aModel();
        when(jpaRepository.save(any(StockPendingItemModel.class))).thenReturn(saved);

        StockPendingItem result = gateway.save(item);

        assertThat(result.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("findById deve retornar o domínio quando encontrado")
    void findById_ShouldReturnDomain_WhenFound() {
        StockPendingItemModel model = aModel();
        when(jpaRepository.findById(model.getId())).thenReturn(Optional.of(model));

        Optional<StockPendingItem> result = gateway.findById(model.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getMaterialId()).isEqualTo(model.getMaterialId());
    }

    @Test
    @DisplayName("findById deve retornar vazio quando não encontrado")
    void findById_ShouldReturnEmpty_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(gateway.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("findByMaterialIdOrderByCreatedAtAsc deve retornar lista convertida")
    void findByMaterialIdOrderByCreatedAtAsc_ShouldReturnConvertedList() {
        UUID materialId = UUID.randomUUID();
        when(jpaRepository.findByMaterialIdOrderByCreatedAtAsc(materialId)).thenReturn(List.of(aModel()));

        List<StockPendingItem> result = gateway.findByMaterialIdOrderByCreatedAtAsc(materialId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("findByServiceOrderId deve retornar lista convertida")
    void findByServiceOrderId_ShouldReturnConvertedList() {
        UUID serviceOrderId = UUID.randomUUID();
        when(jpaRepository.findByServiceOrderId(serviceOrderId)).thenReturn(List.of(aModel(), aModel()));

        List<StockPendingItem> result = gateway.findByServiceOrderId(serviceOrderId);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("deleteById deve delegar pro repositório")
    void deleteById_ShouldDelegateToRepository() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    @DisplayName("delete deve remover pelo id do item de domínio")
    void delete_ShouldRemoveByDomainId() {
        StockPendingItem item = StockPendingItem.create(UUID.randomUUID(), 1, UUID.randomUUID());

        gateway.delete(item);

        verify(jpaRepository).deleteById(item.getId());
    }

    @Test
    @DisplayName("deleteByServiceOrderId deve delegar pro repositório")
    void deleteByServiceOrderId_ShouldDelegateToRepository() {
        UUID serviceOrderId = UUID.randomUUID();

        gateway.deleteByServiceOrderId(serviceOrderId);

        verify(jpaRepository).deleteByServiceOrderId(serviceOrderId);
    }
}
