package com.manish.kitchenpulse.branch;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByCodeIgnoreCase(String code);

    List<Branch> findAllByOrderByCodeAsc();
}
