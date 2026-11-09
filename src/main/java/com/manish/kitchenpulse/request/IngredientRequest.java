package com.manish.kitchenpulse.request;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredient_request")
public class IngredientRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_by_user_id")
    private Long reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 1000)
    private String reviewNote;

    protected IngredientRequest() {}

    IngredientRequest(Long branchId, Long createdByUserId, LocalDateTime createdAt) {
        this.branchId = branchId;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.status = "DRAFT";
    }

    public Long getId() { return id; }
    public Long getBranchId() { return branchId; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public Long getReviewedByUserId() { return reviewedByUserId; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public String getReviewNote() { return reviewNote; }

    void review(String resultStatus, Long reviewerId, LocalDateTime at, String note) {
        if (!"SUBMITTED".equals(status)) {
            throw new IllegalStateException("Only submitted requests can be reviewed");
        }
        status = resultStatus;
        reviewedByUserId = reviewerId;
        reviewedAt = at;
        reviewNote = note;
    }

    void submit(LocalDateTime at) {
        if (!"DRAFT".equals(status)) {
            throw new IllegalStateException("Only draft requests can be submitted");
        }
        status = "SUBMITTED";
        submittedAt = at;
    }
}
