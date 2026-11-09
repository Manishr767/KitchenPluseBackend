package com.manish.kitchenpulse.request;

import java.math.BigDecimal;

public record IngredientRequestItemResponse(Long id, Long ingredientId,
                                            BigDecimal requestedQuantity, BigDecimal approvedQuantity) {
    public static IngredientRequestItemResponse from(IngredientRequestItem item) {
        return new IngredientRequestItemResponse(item.getId(), item.getIngredientId(),
                item.getRequestedQuantity(), item.getApprovedQuantity());
    }
}
