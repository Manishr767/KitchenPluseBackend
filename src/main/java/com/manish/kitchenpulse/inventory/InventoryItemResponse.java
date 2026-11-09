package com.manish.kitchenpulse.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InventoryItemResponse(
        Long ingredientId,
        String ingredientName,
        BigDecimal quantity,
        String unit,
        LocalDateTime countedAt,
        Long stockCountId) {

    static InventoryItemResponse from(StockCountItemRepository.InventoryRow row) {
        return new InventoryItemResponse(row.getIngredientId(), row.getIngredientName(),
                row.getQuantity(), row.getUnitSymbol(), row.getCountedAt(), row.getStockCountId());
    }
}
