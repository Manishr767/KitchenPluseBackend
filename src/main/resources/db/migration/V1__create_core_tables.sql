
-- =====================================================
-- KitchenPulse
-- V1: Create core tables
-- Database: PostgreSQL
-- =====================================================

-- =====================================================
-- 1. BRANCH
-- =====================================================

CREATE TABLE branch (
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                        code VARCHAR(20) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        address VARCHAR(255) NOT NULL,

                        is_active BOOLEAN NOT NULL DEFAULT TRUE,

                        created_at TIMESTAMP NOT NULL
                                                   DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                        updated_at TIMESTAMP NOT NULL
                                                   DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                        CONSTRAINT uq_branch_code UNIQUE (code)
);


-- =====================================================
-- 2. USERS
-- =====================================================

CREATE TABLE users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                       branch_id BIGINT,

                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,

                       role VARCHAR(30) NOT NULL,

                       is_active BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at TIMESTAMP NOT NULL
                                                  DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                       updated_at TIMESTAMP NOT NULL
                                                  DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                       CONSTRAINT fk_users_branch
                           FOREIGN KEY (branch_id)
                               REFERENCES branch(id)
                               ON DELETE RESTRICT,

                       CONSTRAINT chk_users_role
                           CHECK (
                               role IN (
                                        'BRANCH_STAFF',
                                        'CENTRAL_OFFICE_STAFF',
                                        'DELIVERY_PERSON',
                                        'WAREHOUSE_MANAGER',
                                        'ADMIN'
                                   )
                               ),

                       CONSTRAINT chk_users_branch_role
                           CHECK (
                               (role = 'BRANCH_STAFF' AND branch_id IS NOT NULL)
                                   OR
                               (role <> 'BRANCH_STAFF' AND branch_id IS NULL)
                               )
);


-- Case-insensitive unique email.
CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(email));

-- Find users belonging to a branch.
CREATE INDEX idx_users_branch_id
    ON users (branch_id);

-- Filter users by role.
CREATE INDEX idx_users_role
    ON users (role);


-- =====================================================
-- 3. UNIT
-- =====================================================

CREATE TABLE unit (
                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                      name VARCHAR(50) NOT NULL,
                      symbol VARCHAR(20) NOT NULL,

                      created_at TIMESTAMP NOT NULL
                          DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                      CONSTRAINT uq_unit_name UNIQUE (name),
                      CONSTRAINT uq_unit_symbol UNIQUE (symbol)
);


-- =====================================================
-- 4. INGREDIENT
-- =====================================================

CREATE TABLE ingredient (
                            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                            name VARCHAR(100) NOT NULL,
                            stock_unit_id BIGINT NOT NULL,

                            is_active BOOLEAN NOT NULL DEFAULT TRUE,

                            created_at TIMESTAMP NOT NULL
                                                       DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                            updated_at TIMESTAMP NOT NULL
                                                       DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),

                            CONSTRAINT fk_ingredient_stock_unit
                                FOREIGN KEY (stock_unit_id)
                                    REFERENCES unit(id)
                                    ON DELETE RESTRICT,

                            CONSTRAINT chk_ingredient_name_not_blank
                                CHECK (LENGTH(TRIM(name)) > 0)
);


-- Case-insensitive unique ingredient names.
CREATE UNIQUE INDEX uq_ingredient_name_lower
    ON ingredient (LOWER(name));

-- Find ingredients using a particular stock unit.
CREATE INDEX idx_ingredient_stock_unit_id
    ON ingredient (stock_unit_id);
