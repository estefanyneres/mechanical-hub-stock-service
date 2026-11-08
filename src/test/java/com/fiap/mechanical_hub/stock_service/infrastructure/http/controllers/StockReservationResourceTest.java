package com.fiap.mechanical_hub.stock_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.stock_service.application.dto.stock.MaterialQuantityRequest;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.ReservationResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.ReserveMaterialsRequest;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do StockReservationResource")
class StockReservationResourceTest {

    @Mock
    private StockUseCase useCase;

    private StockReservationResource resource;
    private UUID serviceOrderId;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        resource = new StockReservationResource(useCase);
        serviceOrderId = UUID.randomUUID();
        materialId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve responder reserved=true quando todos os itens forem reservados")
    void reserve_ShouldReturnTrue_WhenAllItemsReserved() {
        when(useCase.reserveForServiceOrder(serviceOrderId, materialId, 2)).thenReturn(false);

        var request = new ReserveMaterialsRequest(serviceOrderId, List.of(new MaterialQuantityRequest(materialId, 2)));
        ResponseEntity<ReservationResponse> response = resource.reserve(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().reserved()).isTrue();
        assertThat(response.getBody().motivo()).isNull();
    }

    @Test
    @DisplayName("Deve responder reserved=false e listar o material pendente quando faltar estoque")
    void reserve_ShouldReturnFalseWithMotivo_WhenItemGoesPending() {
        when(useCase.reserveForServiceOrder(serviceOrderId, materialId, 10)).thenReturn(true);

        var request = new ReserveMaterialsRequest(serviceOrderId, List.of(new MaterialQuantityRequest(materialId, 10)));
        ResponseEntity<ReservationResponse> response = resource.reserve(request);

        assertThat(response.getBody().reserved()).isFalse();
        assertThat(response.getBody().motivo()).contains(materialId.toString());
    }

    @Test
    @DisplayName("Deve reservar itens parciais mesmo quando outro item da mesma chamada fica pendente")
    void reserve_ShouldStillCallReserveForEveryItem_EvenIfOneGoesPending() {
        UUID secondMaterialId = UUID.randomUUID();
        when(useCase.reserveForServiceOrder(serviceOrderId, materialId, 1)).thenReturn(true);
        when(useCase.reserveForServiceOrder(serviceOrderId, secondMaterialId, 1)).thenReturn(false);

        var request = new ReserveMaterialsRequest(serviceOrderId, List.of(
                new MaterialQuantityRequest(materialId, 1),
                new MaterialQuantityRequest(secondMaterialId, 1)));

        resource.reserve(request);

        verify(useCase).reserveForServiceOrder(serviceOrderId, materialId, 1);
        verify(useCase).reserveForServiceOrder(serviceOrderId, secondMaterialId, 1);
    }

    @Test
    @DisplayName("Deve dar baixa em cada item na chamada de consumo")
    void consume_ShouldCallRegisterStockOutForEachItem() {
        var request = new ReserveMaterialsRequest(serviceOrderId, List.of(new MaterialQuantityRequest(materialId, 3)));

        ResponseEntity<Void> response = resource.consume(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).registerStockOut(serviceOrderId, materialId, 3);
    }

    @Test
    @DisplayName("Deve inicializar o estoque de um material novo")
    void initialize_ShouldReturnCreated() {
        ResponseEntity<Void> response = resource.initialize(materialId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(useCase).setStockForNewMaterial(materialId);
    }

    @Test
    @DisplayName("Deve remover o estoque de um material excluído")
    void delete_ShouldReturnNoContent() {
        ResponseEntity<Void> response = resource.delete(materialId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(useCase).deleteStockForMaterial(materialId);
    }
}
