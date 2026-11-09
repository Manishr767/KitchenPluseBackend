package com.manish.kitchenpulse.warehouse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
interface WarehouseDispatchRepository extends JpaRepository<WarehouseDispatch,Long>{
 List<WarehouseDispatch> findAllByOrderByDispatchedAtDesc();
 Optional<WarehouseDispatch> findByIngredientRequestId(Long requestId);
}
