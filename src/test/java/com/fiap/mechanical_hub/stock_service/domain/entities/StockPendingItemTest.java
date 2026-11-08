package com.fiap.mechanical_hub.stock_service.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockPendingItemTest {

    @Test
    @DisplayName("create deve gerar item pendente com os dados informados")
    void create_ShouldBuildPendingItem() {
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();

        StockPendingItem item = StockPendingItem.create(serviceOrderId, 8, materialId);

        assertThat(item.getId()).isNotNull();
        assertThat(item.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(item.getMaterialId()).isEqualTo(materialId);
        assertThat(item.getQuantity()).isEqualTo(8);
        assertThat(item.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("reduceQuantity deve subtrair a quantidade informada")
    void reduceQuantity_ShouldDecreaseQuantity() {
        StockPendingItem item = StockPendingItem.create(UUID.randomUUID(), 10, UUID.randomUUID());

        item.reduceQuantity(4);

        assertThat(item.getQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("Construtor completo deve preencher todos os campos")
    void constructor_ShouldSetAllFields() {
        UUID id = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        StockPendingItem item = new StockPendingItem(id, serviceOrderId, materialId, 3, now);

        assertThat(item.getId()).isEqualTo(id);
        assertThat(item.getServiceOrderId()).isEqualTo(serviceOrderId);
        assertThat(item.getMaterialId()).isEqualTo(materialId);
        assertThat(item.getQuantity()).isEqualTo(3);
        assertThat(item.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Construtor vazio deve criar instância sem erro")
    void emptyConstructor_ShouldCreateInstance() {
        StockPendingItem item = new StockPendingItem();

        assertThat(item.getId()).isNull();
    }
}
