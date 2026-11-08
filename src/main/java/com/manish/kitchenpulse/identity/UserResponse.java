package com.manish.kitchenpulse.identity;

import java.time.LocalDateTime;

import com.manish.kitchenpulse.common.security.Role;

/** Deliberately has no password or hash field. */
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        Long branchId,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(),
                u.getBranchId(), u.isActive(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
