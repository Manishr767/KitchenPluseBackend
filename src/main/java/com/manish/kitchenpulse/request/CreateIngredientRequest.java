package com.manish.kitchenpulse.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateIngredientRequest(
        @NotEmpty List<@Valid Item> items) {

    public record Item(
            @NotNull @Positive Long ingredientId,
            @NotNull @DecimalMin(value = "0.001") @Digits(integer = 12, fraction = 3) BigDecimal quantity) {}
}
