-- 1. Orders table
CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        order_number VARCHAR(32) NOT NULL,
                        total_amount NUMERIC(10, 2) NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL
);

-- 2. Order items table
CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             order_id UUID NOT NULL,
                             product_name VARCHAR(128) NOT NULL,
                             quantity INTEGER NOT NULL,
                             notes TEXT[] NOT NULL DEFAULT '{}',
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
                                 REFERENCES orders (id) ON DELETE CASCADE
);

-- 3. Indexes
CREATE INDEX idx_orders_order_number ON orders (order_number);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);