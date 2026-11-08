package com.fiap.mechanical_hub.stock_service.infrastructure.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementModel {

    @Id
    private UUID id;

    @Column(name = "material_id")
    private UUID materialId;

    @Column(name = "service_order_id")
    private UUID serviceOrderId;

    @Column(name = "movement_type", nullable = false, length = 50)
    private String movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
