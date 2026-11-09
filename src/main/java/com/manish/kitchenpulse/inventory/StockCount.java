package com.manish.kitchenpulse.inventory;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_count")
public class StockCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "recorded_by_user_id", nullable = false)
    private Long recordedByUserId;

    @Column(name = "counted_at", nullable = false)
    private LocalDateTime countedAt;

    @Column(nullable = false, length = 20)
    private String status;

    protected StockCount() {
    }

    StockCount(Long branchId, Long recordedByUserId, LocalDateTime countedAt) {
        this.branchId = branchId;
        this.recordedByUserId = recordedByUserId;
        this.countedAt = countedAt;
        this.status = "SUBMITTED";
    }

    public Long getId() { return id; }
    public Long getBranchId() { return branchId; }
    public Long getRecordedByUserId() { return recordedByUserId; }
    public LocalDateTime getCountedAt() { return countedAt; }
    public String getStatus() { return status; }
}
