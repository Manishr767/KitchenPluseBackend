package com.manish.kitchenpulse.warehouse;
import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record CreateWarehouseDispatch(@NotEmpty @Valid List<DispatchItem> items,@Size(max=1000) String note){
 public record DispatchItem(@NotNull @Positive Long ingredientId,@NotNull @DecimalMin("0.001") @Digits(integer=12,fraction=3) BigDecimal dispatchedQuantity){}
}
