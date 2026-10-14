package org.example.kitchenservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO kitchen_tickets (id, order_id, event_id, order_number, items, status)
            VALUES (gen_random_uuid(), :orderId, :eventId, :orderNumber, CAST(:items AS jsonb), :status)
            ON CONFLICT (order_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("orderId") UUID orderId,
                       @Param("eventId") UUID eventId,
                       @Param("orderNumber") String orderNumber,
                       @Param("items") String items,
                       @Param("status") String status);
}
