package com.manish.kitchenpulse.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

interface StockCountRepository extends JpaRepository<StockCount, Long> {
}
