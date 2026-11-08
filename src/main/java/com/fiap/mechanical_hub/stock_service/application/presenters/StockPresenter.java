package com.fiap.mechanical_hub.stock_service.application.presenters;

import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockDetailResponse;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryOutput;
import com.fiap.mechanical_hub.stock_service.application.dto.stock.StockSummaryResponse;

import java.util.List;

public interface StockPresenter {

    List<StockSummaryResponse> toSummaryList(StockSummaryOutput output);

    StockDetailResponse toDetailResponse(StockDetailOutput output);
}
