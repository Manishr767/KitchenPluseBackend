package com.manish.kitchenpulse.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotBlank
        @Size(max = 20)
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "must contain only letters, digits, '-' or '_'")
        String code,

        @NotBlank @Size(max = 100) String name,

        @NotBlank @Size(max = 255) String address) {
}
