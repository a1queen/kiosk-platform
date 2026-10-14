ALTER TABLE kitchen_tickets
ADD status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED';

CREATE INDEX idx_kitchen_tickets_status ON kitchen_tickets (status);