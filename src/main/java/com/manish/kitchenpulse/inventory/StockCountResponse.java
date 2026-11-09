package com.manish.kitchenpulse.inventory;

import java.time.LocalDateTime;

public record StockCountResponse(
        Long stockCountId,
        Long branchId,
        LocalDateTime countedAt,
        String status,
        int itemCount) {
}
