package com.fiap.mechanical_hub.stock_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockMovementResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryResponse;
import com.fiap.mechanical_hub.stock_service.application.presenters.StockPresenter;
import com.fiap.mechanical_hub.stock_service.domain.entities.Stock;
import com.fiap.mechanical_hub.stock_service.domain.entities.StockMovement;
import com.fiap.mechanical_hub.stock_service.domain.enums.StockStatusEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StockPresenterImpl implements StockPresenter {

    @Override
    public List<StockSummaryResponse> toSummaryList(StockSummaryOutput output) {
        Map<UUID, List<Stock>> byMaterial = output.stocks().stream()
                .collect(Collectors.groupingBy(Stock::getMaterialId));

        return byMaterial.entrySet().stream()
                .map(entry -> toSummaryResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public StockDetailResponse toDetailResponse(StockDetailOutput output) {
        int available = sumByStatus(output.stocks(), StockStatusEnum.AVAILABLE);
        int reserved = sumByStatus(output.stocks(), StockStatusEnum.RESERVED);

        List<StockMovementResponse> movements = output.movements().stream()
                .map(this::toMovementResponse)
                .toList();

        return new StockDetailResponse(output.materialId(), available + reserved, available, reserved, movements);
    }

    private StockSummaryResponse toSummaryResponse(UUID materialId, List<Stock> stocks) {
        int available = sumByStatus(stocks, StockStatusEnum.AVAILABLE);
        int reserved = sumByStatus(stocks, StockStatusEnum.RESERVED);
        return new StockSummaryResponse(materialId, available + reserved, available, reserved);
    }

    private int sumByStatus(List<Stock> stocks, StockStatusEnum status) {
        return stocks.stream()
                .filter(s -> s.getStatus() == status)
                .mapToInt(Stock::getQuantity)
                .sum();
    }

    private StockMovementResponse toMovementResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getMaterialId(),
                movement.getServiceOrderId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getCreatedAt()
        );
    }
}
