package com.manish.kitchenpulse.catalog;

import com.manish.kitchenpulse.common.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredient")
public class Ingredient extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Fixed once created: stock data everywhere is expressed in this unit. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_unit_id", nullable = false, updatable = false)
    private Unit stockUnit;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Ingredient() {
        // for JPA
    }

    public Ingredient(String name, Unit stockUnit) {
        this.name = name;
        this.stockUnit = stockUnit;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Unit getStockUnit() {
        return stockUnit;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
