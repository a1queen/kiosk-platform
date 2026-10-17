package org.example.kitchenservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class KitchenTicketController {
    private final KitchenTicketService kitchenTicketService;

    @PatchMapping("/{ticketId}/start-preparation")
    public ResponseEntity<KitchenTicketResponse> startPreparation(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(kitchenTicketService.markAsPreparing(ticketId));
    }

    @PatchMapping("/{ticketId}/mark-ready")
    public ResponseEntity<KitchenTicketResponse> markReady(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(kitchenTicketService.markAsReady(ticketId));
    }

    @PatchMapping("/{ticketId}/complete")
    public ResponseEntity<KitchenTicketResponse> complete(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(kitchenTicketService.markAsCompleted(ticketId));
    }

}
