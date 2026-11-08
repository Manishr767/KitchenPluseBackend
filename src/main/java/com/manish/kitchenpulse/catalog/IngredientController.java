package com.manish.kitchenpulse.catalog;

import com.manish.kitchenpulse.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingredients")
@Tag(name = "Catalog – ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /** Everyone logged in can read the shared catalog. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponse<IngredientResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active,
            @ParameterObject @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ingredientService.list(q, active, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public IngredientResponse get(@PathVariable Long id) {
        return ingredientService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientResponse create(@Valid @RequestBody CreateIngredientRequest request) {
        return ingredientService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientResponse rename(@PathVariable Long id, @Valid @RequestBody UpdateIngredientRequest request) {
        return ingredientService.rename(id, request);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientResponse activate(@PathVariable Long id) {
        return ingredientService.setActive(id, true);
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public IngredientResponse deactivate(@PathVariable Long id) {
        return ingredientService.setActive(id, false);
    }
}
