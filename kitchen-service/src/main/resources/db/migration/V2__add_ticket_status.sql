ALTER TABLE kitchen_tickets
ADD ticket_status VARCHAR(32) NOT NULL DEFAULT 'RECEIVED';

CREATE INDEX idx_kitchen_tickets_status ON kitchen_tickets (ticket_status);