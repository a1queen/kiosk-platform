package org.example.kitchenservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class KitchenTicketController {
    private final KitchenTicketService kitchenTicketService;

    @PatchMapping("/{ticketId}/start-preparation")
    public ResponseEntity<KitchenTicketResponse> changeStatusToPreparing(@PathVariable UUID ticketId) {
        KitchenTicketResponse response = kitchenTicketService.markAsPreparing(ticketId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/mark-ready")
    public ResponseEntity<KitchenTicketResponse> changeStatusToReady(@PathVariable UUID ticketId) {
        KitchenTicketResponse response = kitchenTicketService.markAsReady(ticketId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{ticketId}/complete")
    public ResponseEntity<KitchenTicketResponse> changeStatusToCompleted(@PathVariable UUID ticketId) {
        KitchenTicketResponse response = kitchenTicketService.markAsCompleted(ticketId);
        return ResponseEntity.ok(response);
    }

}
