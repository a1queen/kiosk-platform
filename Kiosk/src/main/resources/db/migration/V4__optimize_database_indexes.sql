-- 1. Optymalizacja sortowania zamowien po dacie:
-- Pobieranie paginacji opiera sie o: ORDER BY created_at DESC
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);

-- Poniewaz usunieta zostala anemiczna metoda w kodzie 'findByStatus', stary indeks per status kosztowal niepotrzebnie:
DROP INDEX IF EXISTS idx_orders_status;

-- 2. Optymalizacja w OutboxEvents - usuniecie nadmiarowych/pokrywajacych sie indeksow na rzecz czesciowego:
DROP INDEX IF EXISTS idx_outbox_created_at;
DROP INDEX IF EXISTS idx_outbox_status_created_at;

-- Ten indeks precyzyjniej oblsluzy odpytywanie publishera 'WHERE status = PENDING ORDER BY created_at':
CREATE INDEX idx_outbox_pending ON outbox_events (created_at ASC) WHERE status = 'PENDING';
