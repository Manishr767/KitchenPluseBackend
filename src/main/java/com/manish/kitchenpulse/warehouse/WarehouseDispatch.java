package com.manish.kitchenpulse.warehouse;
import java.time.LocalDateTime;
import jakarta.persistence.*;
@Entity @Table(name = "warehouse_dispatch")
public class WarehouseDispatch {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @Column(name="ingredient_request_id", nullable=false) private Long ingredientRequestId;
 @Column(name="prepared_by_user_id", nullable=false) private Long preparedByUserId;
 @Column(name="dispatched_at", nullable=false) private LocalDateTime dispatchedAt;
 @Column(length=1000) private String note;
 protected WarehouseDispatch() {}
 WarehouseDispatch(Long requestId, Long userId, LocalDateTime at, String note) { this.ingredientRequestId=requestId; this.preparedByUserId=userId; this.dispatchedAt=at; this.note=note; }
 public Long getId(){return id;} public Long getIngredientRequestId(){return ingredientRequestId;} public Long getPreparedByUserId(){return preparedByUserId;} public LocalDateTime getDispatchedAt(){return dispatchedAt;} public String getNote(){return note;}
}
