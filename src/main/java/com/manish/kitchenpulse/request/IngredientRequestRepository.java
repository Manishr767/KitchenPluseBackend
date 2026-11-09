package com.manish.kitchenpulse.request;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface IngredientRequestRepository extends JpaRepository<IngredientRequest, Long> {
    List<IngredientRequest> findAllByBranchIdOrderByCreatedAtDescIdDesc(Long branchId);
    List<IngredientRequest> findAllByOrderByCreatedAtDescIdDesc();
    Optional<IngredientRequest> findByIdAndBranchId(Long id, Long branchId);
}
