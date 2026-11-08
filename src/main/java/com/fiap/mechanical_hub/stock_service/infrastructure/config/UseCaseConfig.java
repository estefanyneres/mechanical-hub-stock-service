package com.fiap.mechanical_hub.stock_service.infrastructure.config;

import com.fiap.mechanical_hub.stock_service.application.gateways.StockGateway;
import com.fiap.mechanical_hub.stock_service.application.gateways.StockMovementGateway;
import com.fiap.mechanical_hub.stock_service.application.gateways.StockPendingItemGateway;
import com.fiap.mechanical_hub.stock_service.application.presenters.StockPresenter;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockMovementUseCase;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockPendingUseCase;
import com.fiap.mechanical_hub.stock_service.application.usecases.StockUseCase;
import com.fiap.mechanical_hub.stock_service.infrastructure.messaging.SagaEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Casos de uso sao POJOs simples (sem @Component), mesmo padrao do
 * core-service/repair-service -- precisa de um @Bean explicito aqui
 * porque o controller e o SagaEventListener (@Component) precisam
 * injetar uma instancia gerenciada pelo Spring.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public StockMovementUseCase stockMovementUseCase(StockMovementGateway stockMovementGateway) {
        return new StockMovementUseCase(stockMovementGateway);
    }

    @Bean
    public StockPendingUseCase stockPendingUseCase(StockPendingItemGateway stockPendingItemGateway) {
        return new StockPendingUseCase(stockPendingItemGateway);
    }

    @Bean
    public StockUseCase stockUseCase(
            StockMovementUseCase stockMovementUseCase,
            StockPendingUseCase stockPendingUseCase,
            SagaEventPublisher sagaEventPublisher,
            StockMovementGateway stockMovementGateway,
            StockGateway stockGateway,
            StockPresenter stockPresenter) {
        return new StockUseCase(stockMovementUseCase, stockPendingUseCase, sagaEventPublisher,
                stockMovementGateway, stockGateway, stockPresenter);
    }
}
