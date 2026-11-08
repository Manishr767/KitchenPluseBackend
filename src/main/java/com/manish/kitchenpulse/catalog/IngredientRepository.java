package com.manish.kitchenpulse.catalog;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    // The entity graph loads the stock unit in the same query (no N+1 when mapping to DTOs).
    @Override
    @EntityGraph(attributePaths = "stockUnit")
    Optional<Ingredient> findById(Long id);

    @EntityGraph(attributePaths = "stockUnit")
    Page<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = "stockUnit")
    Page<Ingredient> findByActiveAndNameContainingIgnoreCase(boolean active, String name, Pageable pageable);
}
