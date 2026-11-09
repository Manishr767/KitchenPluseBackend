package com.manish.kitchenpulse.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface StockCountItemRepository extends JpaRepository<StockCountItem, Long> {

    interface InventoryRow {
        Long getIngredientId();
        String getIngredientName();
        BigDecimal getQuantity();
        String getUnitSymbol();
        LocalDateTime getCountedAt();
        Long getStockCountId();
    }

    @Query(value = """
            SELECT latest.ingredient_id AS "ingredientId",
                   i.name AS "ingredientName",
                   latest.quantity AS "quantity",
                   u.symbol AS "unitSymbol",
                   latest.counted_at AS "countedAt",
                   latest.stock_count_id AS "stockCountId"
            FROM (
                SELECT DISTINCT ON (sci.ingredient_id)
                       sci.ingredient_id,
                       sci.quantity,
                       sc.counted_at,
                       sc.id AS stock_count_id
                FROM stock_count_item sci
                JOIN stock_count sc ON sc.id = sci.stock_count_id
                WHERE sc.branch_id = :branchId
                  AND sc.status = 'SUBMITTED'
                ORDER BY sci.ingredient_id, sc.counted_at DESC, sc.id DESC
            ) latest
            JOIN ingredient i ON i.id = latest.ingredient_id
            JOIN unit u ON u.id = i.stock_unit_id
            WHERE i.is_active = TRUE
            ORDER BY i.name
            """, nativeQuery = true)
    List<InventoryRow> findLatestInventoryByBranch(@Param("branchId") Long branchId);
}
