package com.fiap.mechanical_hub.stock_service.infrastructure.http.controllers;

import com.fiap.mechanical_hub.stock_service.application.dto.stock.MaterialQuantityRequest;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.ReservationResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.ReserveMaterialsRequest;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Chamado pelo repair-service via DNS interno do cluster, e tambem exposto
 * publicamente pelo API Gateway (igual era /stock/** no monolito) --
 * exige token com perfil ADMINISTRATOR (ver SecurityConfiguration).
 */
@Transactional
@RestController
@RequestMapping("/stock")
@Tag(name = "Estoque", description = "Reserva/baixa sincrona de material, chamada internamente pelo repair-service")
public class StockReservationResource {

    private final StockUseCase useCase;

    public StockReservationResource(StockUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/reservations")
    @Operation(summary = "Reservar material pra uma OS",
            description = "Tenta reservar cada item da lista. Itens sem estoque disponível ficam pendentes " +
                    "(resolvidos depois, automaticamente, quando o estoque for reabastecido) -- não é tudo ou " +
                    "nada, os itens que tinham estoque já ficam reservados mesmo se outro item da mesma chamada " +
                    "ficar pendente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "reserved=true se todos os itens foram reservados, false se algum ficou pendente")
    })
    public ResponseEntity<ReservationResponse> reserve(@RequestBody ReserveMaterialsRequest request) {
        List<String> pendingMaterialIds = new ArrayList<>();

        for (MaterialQuantityRequest item : request.items()) {
            boolean wentPending = useCase.reserveForServiceOrder(
                    request.serviceOrderId(), item.materialId(), item.quantity());
            if (wentPending) {
                pendingMaterialIds.add(item.materialId().toString());
            }
        }

        if (pendingMaterialIds.isEmpty()) {
            return ResponseEntity.ok(new ReservationResponse(true, null));
        }

        String motivo = "Material(is) sem estoque disponível: " + String.join(", ", pendingMaterialIds);
        return ResponseEntity.ok(new ReservationResponse(false, motivo));
    }

    @PostMapping("/reservations/consume")
    @Operation(summary = "Dar baixa em material reservado",
            description = "Chamado quando uma tarefa de execução é finalizada -- decrementa o material " +
                    "reservado pra essa OS, registrando a saída definitiva.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Baixa registrada com sucesso"),
            @ApiResponse(responseCode = "422", description = "Material não está reservado pra essa OS")
    })
    public ResponseEntity<Void> consume(@RequestBody ReserveMaterialsRequest request) {
        for (MaterialQuantityRequest item : request.items()) {
            useCase.registerStockOut(request.serviceOrderId(), item.materialId(), item.quantity());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/materials/{materialId}/initialize")
    @Operation(summary = "Inicializar estoque de um material novo",
            description = "Chamado pelo repair-service quando um material novo é cadastrado no catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estoque inicializado com zero")
    })
    public ResponseEntity<Void> initialize(@PathVariable UUID materialId) {
        useCase.setStockForNewMaterial(materialId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/materials/{materialId}")
    @Operation(summary = "Remover estoque de um material excluído",
            description = "Chamado pelo repair-service quando um material é removido do catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Estoque removido"),
            @ApiResponse(responseCode = "422", description = "Material ainda tem estoque reservado")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID materialId) {
        useCase.deleteStockForMaterial(materialId);
        return ResponseEntity.noContent().build();
    }
}
