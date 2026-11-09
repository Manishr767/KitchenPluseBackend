package com.manish.kitchenpulse.request;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface IngredientRequestItemRepository extends JpaRepository<IngredientRequestItem, Long> {
    List<IngredientRequestItem> findAllByIngredientRequestIdOrderByIdAsc(Long requestId);
}
