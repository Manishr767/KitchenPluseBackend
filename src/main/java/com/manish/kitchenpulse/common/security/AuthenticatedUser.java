package com.manish.kitchenpulse.common.security;

/**
 * The logged-in user as seen by every module. Controllers receive it with
 * {@code @AuthenticationPrincipal AuthenticatedUser user}.
 *
 * @param branchId the user's branch; null for everyone except branch staff
 */
public record AuthenticatedUser(Long id, String email, Role role, Long branchId) {
}
