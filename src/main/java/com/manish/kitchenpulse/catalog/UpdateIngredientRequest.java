package com.manish.kitchenpulse.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Only the name can change. The stock unit is fixed once an ingredient exists. */
public record UpdateIngredientRequest(@NotBlank @Size(max = 100) String name) {
}
