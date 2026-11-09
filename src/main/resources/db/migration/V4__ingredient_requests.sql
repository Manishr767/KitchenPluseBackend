-- Branch ingredient requests. Approval and fulfilment are added in later workflow migrations.
CREATE TABLE ingredient_request (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    created_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    submitted_at TIMESTAMP,
    CONSTRAINT chk_ingredient_request_status CHECK (status IN ('DRAFT', 'SUBMITTED'))
);

CREATE INDEX idx_ingredient_request_branch_created
    ON ingredient_request (branch_id, created_at DESC, id DESC);
CREATE INDEX idx_ingredient_request_status_created
    ON ingredient_request (status, created_at DESC);

CREATE TABLE ingredient_request_item (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ingredient_request_id BIGINT NOT NULL REFERENCES ingredient_request(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredient(id) ON DELETE RESTRICT,
    requested_quantity NUMERIC(15, 3) NOT NULL,
    CONSTRAINT chk_ingredient_request_item_quantity CHECK (requested_quantity > 0),
    CONSTRAINT uq_ingredient_request_item_ingredient UNIQUE (ingredient_request_id, ingredient_id)
);
