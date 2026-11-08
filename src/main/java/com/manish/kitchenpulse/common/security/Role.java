package com.manish.kitchenpulse.common.security;

/**
 * Shared security vocabulary. Names must match the CHECK constraint on
 * users.role and the Spring Security authorities (ROLE_<name>).
 */
public enum Role {
    BRANCH_STAFF,
    CENTRAL_OFFICE_STAFF,
    DELIVERY_PERSON,
    WAREHOUSE_MANAGER,
    ADMIN
}
