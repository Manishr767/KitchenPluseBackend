package com.manish.kitchenpulse.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewIngredientRequest(
        @NotEmpty List<@Valid ReviewItem> items,
        @Size(max = 1000) String reviewNote) {

    public record ReviewItem(
            @NotNull Long ingredientId,
            @NotNull BigDecimal approvedQuantity) {
    }
}
