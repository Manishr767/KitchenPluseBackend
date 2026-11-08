package com.manish.kitchenpulse.catalog;

import java.time.LocalDateTime;

public record IngredientResponse(
        Long id,
        String name,
        UnitResponse stockUnit,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    static IngredientResponse from(Ingredient i) {
        return new IngredientResponse(i.getId(), i.getName(), UnitResponse.from(i.getStockUnit()),
                i.isActive(), i.getCreatedAt(), i.getUpdatedAt());
    }
}
