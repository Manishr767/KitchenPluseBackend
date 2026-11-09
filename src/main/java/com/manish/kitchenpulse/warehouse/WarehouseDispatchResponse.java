package com.manish.kitchenpulse.warehouse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
public record WarehouseDispatchResponse(Long id,Long ingredientRequestId,Long preparedByUserId,LocalDateTime dispatchedAt,String note,List<Item> items){
 public record Item(Long id,Long ingredientId,BigDecimal approvedQuantity,BigDecimal dispatchedQuantity){}
}
