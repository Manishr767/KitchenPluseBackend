package com.manish.kitchenpulse.warehouse;
import java.util.List;
import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import com.manish.kitchenpulse.request.IngredientRequestResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/warehouse") @Tag(name="Warehouse")
public class WarehouseController {
 private final WarehouseService service; WarehouseController(WarehouseService s){service=s;}
 @GetMapping("/approved-requests") @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
 public List<IngredientRequestResponse> approved(@AuthenticationPrincipal AuthenticatedUser user){return service.approvedRequests(user);}
 @GetMapping("/dispatches") @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
 public List<WarehouseDispatchResponse> history(@AuthenticationPrincipal AuthenticatedUser user){return service.history(user);}
 @PostMapping("/requests/{requestId}/dispatch") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE_MANAGER')")
 public WarehouseDispatchResponse dispatch(@PathVariable Long requestId,@Valid @RequestBody CreateWarehouseDispatch body,@AuthenticationPrincipal AuthenticatedUser user){return service.dispatch(requestId,body,user);}
}
