package com.fiap.mechanical_hub.stock_service.domain.entities;

import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import com.fiap.mechanical_hub.stock_service.domain.exceptions.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @Test
    @DisplayName("Deve criar estoque reservado com status RESERVED")
    void createReservedStock_ShouldSetStatusReserved() {
        UUID materialId = UUID.randomUUID();
        Stock stock = Stock.createReservedStock(materialId, 10);

        assertThat(stock.getId()).isNotNull();
        assertThat(stock.getMaterialId()).isEqualTo(materialId);
        assertThat(stock.getQuantity()).isEqualTo(10);
        assertThat(stock.getStatus()).isEqualTo(StockStatusEnum.RESERVED);
        assertThat(stock.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar estoque zerado e disponível para material novo")
    void setStockForNewMaterial_ShouldStartAtZeroAvailable() {
        UUID materialId = UUID.randomUUID();
        Stock stock = Stock.setStockForNewMaterial(materialId);

        assertThat(stock.getQuantity()).isZero();
        assertThat(stock.getStatus()).isEqualTo(StockStatusEnum.AVAILABLE);
    }

    @Test
    @DisplayName("Deve adicionar quantidade")
    void addQuantity_ShouldIncreaseQuantity() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());

        stock.addQuantity(5);

        assertThat(stock.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao adicionar quantidade negativa")
    void addQuantity_ShouldThrow_WhenNegative() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());

        assertThatThrownBy(() -> stock.addQuantity(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser negativa");
    }

    @Test
    @DisplayName("Deve subtrair quantidade")
    void subtractQuantity_ShouldDecreaseQuantity() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 10);

        stock.subtractQuantity(4);

        assertThat(stock.getQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException ao subtrair mais do que existe")
    void subtractQuantity_ShouldThrow_WhenInsufficient() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 3);

        assertThatThrownBy(() -> stock.subtractQuantity(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("insuficiente");
    }

    @Test
    @DisplayName("checkMaterialAvailability deve retornar true quando disponível mas insuficiente")
    void checkMaterialAvailability_ShouldReturnTrue_WhenAvailableButInsufficient() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());
        stock.addQuantity(2);

        assertThat(stock.checkMaterialAvailability(5)).isTrue();
    }

    @Test
    @DisplayName("checkMaterialAvailability deve retornar false quando quantidade é suficiente")
    void checkMaterialAvailability_ShouldReturnFalse_WhenSufficient() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());
        stock.addQuantity(10);

        assertThat(stock.checkMaterialAvailability(5)).isFalse();
    }

    @Test
    @DisplayName("checkMaterialAvailability deve retornar false quando status não é AVAILABLE")
    void checkMaterialAvailability_ShouldReturnFalse_WhenNotAvailable() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 1);

        assertThat(stock.checkMaterialAvailability(5)).isFalse();
    }

    @Test
    @DisplayName("Deve liberar quantidade reservada")
    void release_ShouldDecreaseQuantity() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 10);

        stock.release(4);

        assertThat(stock.getQuantity()).isEqualTo(6);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao liberar mais do que está reservado")
    void release_ShouldThrow_WhenInsufficient() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 2);

        assertThatThrownBy(() -> stock.release(5))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("insuficiente");
    }

    @Test
    @DisplayName("Deve repor quantidade")
    void replenish_ShouldIncreaseQuantity() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());

        stock.replenish(7);

        assertThat(stock.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Deve dar saída em item reservado")
    void decreaseReserved_ShouldDecreaseQuantity_WhenReserved() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 10);

        stock.decreaseReserved(3);

        assertThat(stock.getQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao dar saída em item não reservado")
    void decreaseReserved_ShouldThrow_WhenNotReserved() {
        Stock stock = Stock.setStockForNewMaterial(UUID.randomUUID());
        stock.addQuantity(10);

        assertThatThrownBy(() -> stock.decreaseReserved(3))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não está reservado");
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao dar saída com quantidade reservada insuficiente")
    void decreaseReserved_ShouldThrow_WhenInsufficient() {
        Stock stock = Stock.createReservedStock(UUID.randomUUID(), 2);

        assertThatThrownBy(() -> stock.decreaseReserved(5))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("insuficiente para dar saída");
    }

    @Test
    @DisplayName("Construtor completo deve preencher todos os campos")
    void constructor_ShouldSetAllFields() {
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        var now = java.time.LocalDateTime.now();

        Stock stock = new Stock(id, materialId, 5, StockStatusEnum.AVAILABLE, now);

        assertThat(stock.getId()).isEqualTo(id);
        assertThat(stock.getMaterialId()).isEqualTo(materialId);
        assertThat(stock.getQuantity()).isEqualTo(5);
        assertThat(stock.getStatus()).isEqualTo(StockStatusEnum.AVAILABLE);
        assertThat(stock.getUpdatedAt()).isEqualTo(now);
    }
}
