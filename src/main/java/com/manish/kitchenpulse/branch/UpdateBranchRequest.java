package com.manish.kitchenpulse.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The branch code is a stable identifier and cannot be changed. */
public record UpdateBranchRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String address) {
}
