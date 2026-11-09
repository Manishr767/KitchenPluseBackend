ALTER TABLE ingredient_request
    DROP CONSTRAINT chk_ingredient_request_status,
    ADD COLUMN reviewed_by_user_id BIGINT REFERENCES users(id) ON DELETE RESTRICT,
    ADD COLUMN reviewed_at TIMESTAMP,
    ADD COLUMN review_note VARCHAR(1000),
    ADD CONSTRAINT chk_ingredient_request_status CHECK (
        status IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'PARTIALLY_APPROVED', 'REJECTED')
    );

ALTER TABLE ingredient_request_item
    ADD COLUMN approved_quantity NUMERIC(15, 3),
    ADD CONSTRAINT chk_ingredient_request_item_approved_quantity
        CHECK (approved_quantity IS NULL OR (approved_quantity >= 0 AND approved_quantity <= requested_quantity));

CREATE INDEX idx_ingredient_request_review_queue
    ON ingredient_request (status, submitted_at ASC)
    WHERE status = 'SUBMITTED';
