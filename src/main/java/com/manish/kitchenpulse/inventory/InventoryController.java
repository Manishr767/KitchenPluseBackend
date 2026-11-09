package com.manish.kitchenpulse.inventory;

import java.util.List;

import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/api/inventory")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_STAFF','CENTRAL_OFFICE_STAFF','WAREHOUSE_MANAGER')")
    public List<InventoryItemResponse> getInventory(
            @RequestParam(required = false) Long branchId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return inventoryService.getInventory(branchId, user);
    }

    @PostMapping("/api/stock-counts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BRANCH_STAFF')")
    public StockCountResponse recordStockCount(
            @Valid @RequestBody CreateStockCountRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return inventoryService.recordStockCount(request, user);
    }
}
