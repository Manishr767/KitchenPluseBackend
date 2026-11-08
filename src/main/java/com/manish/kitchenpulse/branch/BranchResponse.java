package com.manish.kitchenpulse.branch;

import java.time.LocalDateTime;

public record BranchResponse(
        Long id,
        String code,
        String name,
        String address,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    static BranchResponse from(Branch b) {
        return new BranchResponse(b.getId(), b.getCode(), b.getName(), b.getAddress(),
                b.isActive(), b.getCreatedAt(), b.getUpdatedAt());
    }
}
