package org.example.kitchenservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, UUID> {

    Optional<KitchenTicket> findByOrderId(UUID orderId);

    @Modifying
    @Query(value = """
            INSERT INTO kitchen_tickets (id, order_id, event_id, order_number, items, ticket_status)
            VALUES (gen_random_uuid(), :orderId, :eventId, :orderNumber, CAST(:items AS jsonb), :ticket_status)
            ON CONFLICT (order_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("orderId") UUID orderId,
                       @Param("eventId") UUID eventId,
                       @Param("orderNumber") String orderNumber,
                       @Param("items") String items,
                       @Param("ticket_status") String ticket_status);



}
