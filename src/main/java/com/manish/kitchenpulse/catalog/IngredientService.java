package com.manish.kitchenpulse.catalog;

import com.manish.kitchenpulse.common.BusinessRuleException;
import com.manish.kitchenpulse.common.ConflictException;
import com.manish.kitchenpulse.common.NotFoundException;
import com.manish.kitchenpulse.common.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public API of the catalog module (shared ingredient list used by every branch).
 */
@Service
@Transactional(readOnly = true)
public class IngredientService {

    private final IngredientRepository ingredients;
    private final UnitRepository units;

    IngredientService(IngredientRepository ingredients, UnitRepository units) {
        this.ingredients = ingredients;
        this.units = units;
    }

    public PageResponse<IngredientResponse> list(String nameFilter, Boolean active, Pageable pageable) {
        String q = nameFilter == null ? "" : nameFilter.trim();
        Page<Ingredient> page = (active == null)
                ? ingredients.findByNameContainingIgnoreCase(q, pageable)
                : ingredients.findByActiveAndNameContainingIgnoreCase(active, q, pageable);
        return PageResponse.from(page.map(IngredientResponse::from));
    }

    public IngredientResponse get(Long id) {
        return IngredientResponse.from(find(id));
    }

    @Transactional
    public IngredientResponse create(CreateIngredientRequest request) {
        String name = request.name().trim();
        if (name.isEmpty()) {
            throw new BusinessRuleException("Ingredient name must not be blank");
        }
        if (ingredients.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Ingredient '" + name + "' already exists");
        }
        Unit unit = units.findById(request.stockUnitId())
                .orElseThrow(() -> new BusinessRuleException(
                        "Unit " + request.stockUnitId() + " does not exist"));
        Ingredient saved = ingredients.saveAndFlush(new Ingredient(name, unit));
        return IngredientResponse.from(saved);
    }

    @Transactional
    public IngredientResponse rename(Long id, UpdateIngredientRequest request) {
        Ingredient ingredient = find(id);
        String name = request.name().trim();
        if (name.isEmpty()) {
            throw new BusinessRuleException("Ingredient name must not be blank");
        }
        if (ingredients.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Ingredient '" + name + "' already exists");
        }
        ingredient.setName(name);
        return IngredientResponse.from(ingredient);
    }

    @Transactional
    public IngredientResponse setActive(Long id, boolean active) {
        Ingredient ingredient = find(id);
        ingredient.setActive(active);
        return IngredientResponse.from(ingredient);
    }

    /** For other modules: fails with a business-rule error unless the ingredient exists and is active. */
    public void requireActive(Long id) {
        Ingredient ingredient = ingredients.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Ingredient " + id + " does not exist"));
        if (!ingredient.isActive()) {
            throw new BusinessRuleException("Ingredient '" + ingredient.getName() + "' is inactive");
        }
    }

    private Ingredient find(Long id) {
        return ingredients.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient " + id + " not found"));
    }
}
