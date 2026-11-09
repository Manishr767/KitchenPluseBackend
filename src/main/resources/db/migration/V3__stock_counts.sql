-- Periodic branch stock counts. Inventory is derived from the latest
-- submitted count per ingredient and branch; withdrawals are not tracked.
CREATE TABLE stock_count (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch(id) ON DELETE RESTRICT,
    recorded_by_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    counted_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_stock_count_status CHECK (status IN ('SUBMITTED'))
);

CREATE INDEX idx_stock_count_branch_counted_at
    ON stock_count (branch_id, counted_at DESC, id DESC);

CREATE TABLE stock_count_item (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stock_count_id BIGINT NOT NULL REFERENCES stock_count(id) ON DELETE CASCADE,
    ingredient_id BIGINT NOT NULL REFERENCES ingredient(id) ON DELETE RESTRICT,
    quantity NUMERIC(15, 3) NOT NULL,
    CONSTRAINT chk_stock_count_item_quantity CHECK (quantity >= 0),
    CONSTRAINT uq_stock_count_item_ingredient UNIQUE (stock_count_id, ingredient_id)
);

CREATE INDEX idx_stock_count_item_ingredient
    ON stock_count_item (ingredient_id);
