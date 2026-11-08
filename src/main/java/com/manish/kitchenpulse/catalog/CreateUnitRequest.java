package com.manish.kitchenpulse.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUnitRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 20) String symbol) {
}
