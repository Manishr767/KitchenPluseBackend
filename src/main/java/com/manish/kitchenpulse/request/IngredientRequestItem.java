package com.manish.kitchenpulse.request;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredient_request_item")
public class IngredientRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_request_id", nullable = false)
    private Long ingredientRequestId;

    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @Column(name = "requested_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal requestedQuantity;

    protected IngredientRequestItem() {}

    IngredientRequestItem(Long requestId, Long ingredientId, BigDecimal quantity) {
        this.ingredientRequestId = requestId;
        this.ingredientId = ingredientId;
        this.requestedQuantity = quantity;
    }

    public Long getId() { return id; }
    public Long getIngredientRequestId() { return ingredientRequestId; }
    public Long getIngredientId() { return ingredientId; }
    public BigDecimal getRequestedQuantity() { return requestedQuantity; }
}
