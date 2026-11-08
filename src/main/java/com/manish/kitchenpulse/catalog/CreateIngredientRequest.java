package com.manish.kitchenpulse.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIngredientRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long stockUnitId) {
}
