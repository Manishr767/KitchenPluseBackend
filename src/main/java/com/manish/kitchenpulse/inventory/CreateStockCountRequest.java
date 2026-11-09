package com.manish.kitchenpulse.inventory;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateStockCountRequest(
        @NotNull @Positive Long branchId,
        @NotEmpty List<@Valid StockCountLineRequest> items) {

    public record StockCountLineRequest(
            @NotNull @Positive Long ingredientId,
            @NotNull @DecimalMin("0.0") @Digits(integer = 12, fraction = 3) BigDecimal quantity) {
    }
}
