package com.manish.kitchenpulse.catalog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface UnitRepository extends JpaRepository<Unit, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsBySymbolIgnoreCase(String symbol);

    List<Unit> findAllByOrderByNameAsc();
}
