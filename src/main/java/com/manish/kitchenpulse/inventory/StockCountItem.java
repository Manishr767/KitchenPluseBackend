package com.manish.kitchenpulse.inventory;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_count_item")
public class StockCountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_count_id", nullable = false)
    private Long stockCountId;

    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    protected StockCountItem() {
    }

    StockCountItem(Long stockCountId, Long ingredientId, BigDecimal quantity) {
        this.stockCountId = stockCountId;
        this.ingredientId = ingredientId;
        this.quantity = quantity;
    }
}
