package com.fiap.mechanical_hub.stock_service.infrastructure.database.gateways.impl;

import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.models.StockModel;
import com.fiap.mechanical_hub.stock_service.infrastructure.database.repositories.StockJpaRepository;
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
class StockGatewayImplTest {

    @Mock
    private StockJpaRepository jpaRepository;

    private StockGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new StockGatewayImpl(jpaRepository);
    }

    private StockModel aModel() {
        return new StockModel(UUID.randomUUID(), UUID.randomUUID(), 10, StockStatusEnum.AVAILABLE, LocalDateTime.now());
    }

    @Test
    @DisplayName("save deve persistir e retornar o domínio convertido")
    void save_ShouldPersistAndReturnDomain() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());
        StockModel savedModel = aModel();
        when(jpaRepository.save(any(StockModel.class))).thenReturn(savedModel);

        Stock result = gateway.save(stock);

        assertThat(result.getId()).isEqualTo(savedModel.getId());
        verify(jpaRepository).save(any(StockModel.class));
    }

    @Test
    @DisplayName("findByMaterialId deve retornar o domínio quando encontrado")
    void findByMaterialId_ShouldReturnDomain_WhenFound() {
        StockModel model = aModel();
        when(jpaRepository.findFirstByMaterialId(model.getMaterialId())).thenReturn(Optional.of(model));

        Optional<Stock> result = gateway.findByMaterialId(model.getMaterialId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(model.getId());
    }

    @Test
    @DisplayName("findByMaterialId deve retornar vazio quando não encontrado")
    void findByMaterialId_ShouldReturnEmpty_WhenNotFound() {
        UUID materialId = UUID.randomUUID();
        when(jpaRepository.findFirstByMaterialId(materialId)).thenReturn(Optional.empty());

        assertThat(gateway.findByMaterialId(materialId)).isEmpty();
    }

    @Test
    @DisplayName("findById deve retornar o domínio quando encontrado")
    void findById_ShouldReturnDomain_WhenFound() {
        StockModel model = aModel();
        when(jpaRepository.findById(model.getId())).thenReturn(Optional.of(model));

        Optional<Stock> result = gateway.findById(model.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getMaterialId()).isEqualTo(model.getMaterialId());
    }

    @Test
    @DisplayName("findByMaterialIdAndStatus deve retornar o domínio quando encontrado")
    void findByMaterialIdAndStatus_ShouldReturnDomain_WhenFound() {
        StockModel model = aModel();
        when(jpaRepository.findByMaterialIdAndStatus(model.getMaterialId(), StockStatusEnum.AVAILABLE))
                .thenReturn(Optional.of(model));

        Optional<Stock> result = gateway.findByMaterialIdAndStatus(model.getMaterialId(), StockStatusEnum.AVAILABLE);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("findAll deve retornar lista convertida")
    void findAll_ShouldReturnConvertedList() {
        when(jpaRepository.findAll()).thenReturn(List.of(aModel(), aModel()));

        List<Stock> result = gateway.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findAllByMaterialId deve retornar lista convertida")
    void findAllByMaterialId_ShouldReturnConvertedList() {
        UUID materialId = UUID.randomUUID();
        when(jpaRepository.findAllByMaterialId(materialId)).thenReturn(List.of(aModel()));

        List<Stock> result = gateway.findAllByMaterialId(materialId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("deleteById deve delegar pro repositório")
    void deleteById_ShouldDelegateToRepository() {
        UUID id = UUID.randomUUID();

        gateway.deleteById(id);

        verify(jpaRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteByMaterialId deve delegar pro repositório")
    void deleteByMaterialId_ShouldDelegateToRepository() {
        UUID materialId = UUID.randomUUID();

        gateway.deleteByMaterialId(materialId);

        verify(jpaRepository).deleteByMaterialId(materialId);
    }

    @Test
    @DisplayName("initializeForMaterial deve salvar um estoque zerado disponível")
    void initializeForMaterial_ShouldSaveAvailableZeroStock() {
        UUID materialId = UUID.randomUUID();
        when(jpaRepository.save(any(StockModel.class))).thenReturn(aModel());

        gateway.initializeForMaterial(materialId);

        verify(jpaRepository).save(argThat(m ->
                m.getMaterialId().equals(materialId)
                        && m.getQuantity() == 0
                        && m.getStatus() == StockStatusEnum.AVAILABLE));
    }
}
