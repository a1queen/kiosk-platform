CREATE TABLE kitchen_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL ,
    event_id UUID NOT NULL ,
    order_number VARCHAR(32) NOT NULL ,
    items JSONB NOT NULL ,
    received_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_kitchen_tickets_order_id UNIQUE (order_id)
);
