package com.manish.kitchenpulse.inventory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.manish.kitchenpulse.branch.BranchService;
import com.manish.kitchenpulse.catalog.IngredientService;
import com.manish.kitchenpulse.common.BusinessRuleException;
import com.manish.kitchenpulse.common.security.AuthenticatedUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final BranchService branches;
    private final IngredientService ingredients;
    private final StockCountRepository stockCounts;
    private final StockCountItemRepository stockCountItems;

    InventoryService(BranchService branches, IngredientService ingredients,
                     StockCountRepository stockCounts, StockCountItemRepository stockCountItems) {
        this.branches = branches;
        this.ingredients = ingredients;
        this.stockCounts = stockCounts;
        this.stockCountItems = stockCountItems;
    }

    public List<InventoryItemResponse> getInventory(Long branchId, AuthenticatedUser user) {
        Long permittedBranchId = resolveBranch(branchId, user);
        return stockCountItems.findLatestInventoryByBranch(permittedBranchId).stream()
                .map(InventoryItemResponse::from).toList();
    }

    @Transactional
    public StockCountResponse recordStockCount(CreateStockCountRequest request, AuthenticatedUser user) {
        Long branchId = resolveBranch(request.branchId(), user);
        branches.requireActive(branchId);

        Set<Long> seenIngredientIds = new HashSet<>();
        for (CreateStockCountRequest.StockCountLineRequest item : request.items()) {
            if (!seenIngredientIds.add(item.ingredientId())) {
                throw new BusinessRuleException("Ingredient " + item.ingredientId()
                        + " appears more than once in this stock count");
            }
            ingredients.requireActive(item.ingredientId());
        }

        LocalDateTime countedAt = LocalDateTime.now(ZoneOffset.UTC);
        StockCount saved = stockCounts.saveAndFlush(
                new StockCount(branchId, user.id(), countedAt));

        List<StockCountItem> lines = request.items().stream()
                .map(item -> new StockCountItem(saved.getId(), item.ingredientId(), item.quantity()))
                .toList();
        stockCountItems.saveAll(lines);

        return new StockCountResponse(saved.getId(), branchId, countedAt, "SUBMITTED", lines.size());
    }

    private Long resolveBranch(Long requestedBranchId, AuthenticatedUser user) {
        if (user.branchId() != null) {
            if (requestedBranchId != null && !requestedBranchId.equals(user.branchId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Branch staff can only access their own branch");
            }
            return user.branchId();
        }
        if (requestedBranchId == null) {
            throw new BusinessRuleException("branchId is required for this user");
        }
        return requestedBranchId;
    }
}
