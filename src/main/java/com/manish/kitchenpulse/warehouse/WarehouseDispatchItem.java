package com.manish.kitchenpulse.warehouse;
import java.math.BigDecimal;
import jakarta.persistence.*;
@Entity @Table(name="warehouse_dispatch_item")
public class WarehouseDispatchItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="warehouse_dispatch_id",nullable=false) private Long warehouseDispatchId;
 @Column(name="ingredient_id",nullable=false) private Long ingredientId;
 @Column(name="approved_quantity",nullable=false,precision=15,scale=3) private BigDecimal approvedQuantity;
 @Column(name="dispatched_quantity",nullable=false,precision=15,scale=3) private BigDecimal dispatchedQuantity;
 protected WarehouseDispatchItem() {}
 WarehouseDispatchItem(Long dispatchId,Long ingredientId,BigDecimal approved,BigDecimal dispatched){this.warehouseDispatchId=dispatchId;this.ingredientId=ingredientId;this.approvedQuantity=approved;this.dispatchedQuantity=dispatched;}
 public Long getId(){return id;} public Long getWarehouseDispatchId(){return warehouseDispatchId;} public Long getIngredientId(){return ingredientId;} public BigDecimal getApprovedQuantity(){return approvedQuantity;} public BigDecimal getDispatchedQuantity(){return dispatchedQuantity;}
}
