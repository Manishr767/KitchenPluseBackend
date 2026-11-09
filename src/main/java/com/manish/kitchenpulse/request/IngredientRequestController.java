package com.manish.kitchenpulse.request;

import java.util.List;

import com.manish.kitchenpulse.common.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredient-requests")
@Tag(name = "Ingredient requests")
public class IngredientRequestController {

    private final IngredientRequestService service;

    IngredientRequestController(IngredientRequestService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BRANCH_STAFF')")
    public IngredientRequestResponse create(@Valid @RequestBody CreateIngredientRequest body,
                                            @AuthenticationPrincipal AuthenticatedUser user) {
        return service.create(body, user);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_STAFF','CENTRAL_OFFICE_STAFF','WAREHOUSE_MANAGER')")
    public List<IngredientRequestResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.list(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_STAFF','CENTRAL_OFFICE_STAFF','WAREHOUSE_MANAGER')")
    public IngredientRequestResponse get(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return service.get(id, user);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('BRANCH_STAFF')")
    public IngredientRequestResponse submit(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        return service.submit(id, user);
    }
}
