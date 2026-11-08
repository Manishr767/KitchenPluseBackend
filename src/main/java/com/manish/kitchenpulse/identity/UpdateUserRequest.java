package com.manish.kitchenpulse.identity;

import com.manish.kitchenpulse.common.security.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Email is the login name and is not editable here. */
public record UpdateUserRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Role role,
        Long branchId) {
}
