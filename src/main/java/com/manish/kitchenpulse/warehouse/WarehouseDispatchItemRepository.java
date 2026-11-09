package com.manish.kitchenpulse.warehouse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
interface WarehouseDispatchItemRepository extends JpaRepository<WarehouseDispatchItem,Long>{
 List<WarehouseDispatchItem> findAllByWarehouseDispatchIdOrderByIdAsc(Long dispatchId);
}
