package com.manish.kitchenpulse.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Set;

import com.manish.kitchenpulse.branch.BranchService;
import com.manish.kitchenpulse.catalog.IngredientService;
import com.manish.kitchenpulse.common.BusinessRuleException;
import com.manish.kitchenpulse.common.NotFoundException;
import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import com.manish.kitchenpulse.common.security.Role;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class IngredientRequestService {

    private final BranchService branches;
    private final IngredientService ingredients;
    private final IngredientRequestRepository requests;
    private final IngredientRequestItemRepository items;

    IngredientRequestService(BranchService branches, IngredientService ingredients,
                             IngredientRequestRepository requests, IngredientRequestItemRepository items) {
        this.branches = branches;
        this.ingredients = ingredients;
        this.requests = requests;
        this.items = items;
    }

    @Transactional
    public IngredientRequestResponse create(CreateIngredientRequest body, AuthenticatedUser user) {
        if (user.role() != Role.BRANCH_STAFF || user.branchId() == null) {
            throw new AccessDeniedException("Only branch staff can create ingredient requests");
        }
        branches.requireActive(user.branchId());
        Set<Long> seen = new HashSet<>();
        body.items().forEach(item -> {
            if (!seen.add(item.ingredientId())) {
                throw new BusinessRuleException("Ingredient " + item.ingredientId() + " appears more than once");
            }
            ingredients.requireActive(item.ingredientId());
        });

        IngredientRequest saved = requests.saveAndFlush(
                new IngredientRequest(user.branchId(), user.id(), LocalDateTime.now(ZoneOffset.UTC)));
        List<IngredientRequestItem> savedItems = body.items().stream()
                .map(item -> new IngredientRequestItem(saved.getId(), item.ingredientId(), item.quantity()))
                .toList();
        items.saveAll(savedItems);
        return response(saved, savedItems);
    }

    public List<IngredientRequestResponse> list(AuthenticatedUser user) {
        List<IngredientRequest> found = user.role() == Role.BRANCH_STAFF
                ? requests.findAllByBranchIdOrderByCreatedAtDescIdDesc(user.branchId())
                : requests.findAllByOrderByCreatedAtDescIdDesc();
        return found.stream().map(this::response).toList();
    }

    public IngredientRequestResponse get(Long id, AuthenticatedUser user) {
        IngredientRequest request = findVisible(id, user);
        return response(request);
    }

    @Transactional
    public IngredientRequestResponse submit(Long id, AuthenticatedUser user) {
        IngredientRequest request = findVisible(id, user);
        if (user.role() != Role.BRANCH_STAFF) {
            throw new AccessDeniedException("Only branch staff can submit ingredient requests");
        }
        if (!"DRAFT".equals(request.getStatus())) {
            throw new BusinessRuleException("Only draft requests can be submitted");
        }
        request.submit(LocalDateTime.now(ZoneOffset.UTC));
        return response(request);
    }

    @Transactional
    public IngredientRequestResponse review(Long id, ReviewIngredientRequest body, AuthenticatedUser user) {
        if (user.role() != Role.CENTRAL_OFFICE_STAFF && user.role() != Role.ADMIN) {
            throw new AccessDeniedException("Only Central Office staff can review ingredient requests");
        }
        IngredientRequest request = requests.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient request " + id + " not found"));
        if (!"SUBMITTED".equals(request.getStatus())) {
            throw new BusinessRuleException("Only submitted requests can be reviewed");
        }

        List<IngredientRequestItem> requestItems = items.findAllByIngredientRequestIdOrderByIdAsc(id);
        Map<Long, IngredientRequestItem> byIngredient = requestItems.stream()
                .collect(Collectors.toMap(IngredientRequestItem::getIngredientId, Function.identity()));
        Map<Long, BigDecimal> approved = new java.util.HashMap<>();
        for (ReviewIngredientRequest.ReviewItem line : body.items()) {
            if (approved.putIfAbsent(line.ingredientId(), line.approvedQuantity()) != null) {
                throw new BusinessRuleException("An ingredient can only appear once in a review");
            }
            IngredientRequestItem requestItem = byIngredient.get(line.ingredientId());
            if (requestItem == null) {
                throw new BusinessRuleException("Ingredient " + line.ingredientId() + " is not in this request");
            }
            BigDecimal quantity = line.approvedQuantity();
            if (quantity.signum() < 0 || quantity.scale() > 3
                    || quantity.compareTo(requestItem.getRequestedQuantity()) > 0) {
                throw new BusinessRuleException("Approved quantity must be between zero and requested quantity (maximum 3 decimal places)");
            }
        }
        if (approved.size() != requestItems.size()) {
            throw new BusinessRuleException("Review must include every requested ingredient");
        }

        boolean allFull = true;
        boolean anyApproved = false;
        for (IngredientRequestItem item : requestItems) {
            BigDecimal quantity = approved.get(item.getIngredientId());
            item.approve(quantity);
            anyApproved |= quantity.signum() > 0;
            allFull &= quantity.compareTo(item.getRequestedQuantity()) == 0;
        }
        String result = !anyApproved ? "REJECTED" : allFull ? "APPROVED" : "PARTIALLY_APPROVED";
        String note = body.reviewNote() == null || body.reviewNote().isBlank() ? null : body.reviewNote().trim();
        request.review(result, user.id(), LocalDateTime.now(ZoneOffset.UTC), note);
        return response(request, requestItems);
    }

    private IngredientRequest findVisible(Long id, AuthenticatedUser user) {
        if (user.role() == Role.BRANCH_STAFF) {
            return requests.findByIdAndBranchId(id, user.branchId())
                    .orElseThrow(() -> new NotFoundException("Ingredient request " + id + " not found"));
        }
        if (user.role() != Role.ADMIN && user.role() != Role.CENTRAL_OFFICE_STAFF
                && user.role() != Role.WAREHOUSE_MANAGER) {
            throw new AccessDeniedException("You cannot view ingredient requests");
        }
        return requests.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient request " + id + " not found"));
    }

    private IngredientRequestResponse response(IngredientRequest request) {
        return response(request, items.findAllByIngredientRequestIdOrderByIdAsc(request.getId()));
    }

    private IngredientRequestResponse response(IngredientRequest request, List<IngredientRequestItem> lines) {
        return new IngredientRequestResponse(request.getId(), request.getBranchId(), request.getCreatedByUserId(),
                request.getStatus(), request.getCreatedAt(), request.getSubmittedAt(), request.getReviewedByUserId(),
                request.getReviewedAt(), request.getReviewNote(),
                lines.stream().map(IngredientRequestItemResponse::from).toList());
    }
}
