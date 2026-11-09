package com.manish.kitchenpulse.request;

import java.time.LocalDateTime;
import java.util.List;

public record IngredientRequestResponse(
        Long id,
        Long branchId,
        Long createdByUserId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime submittedAt,
        List<IngredientRequestItemResponse> items) {
}
