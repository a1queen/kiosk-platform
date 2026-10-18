package org.example.kitchenservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.kitchenservice.dto.KitchenTicketResponse;
import org.example.kitchenservice.service.KitchenTicketService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping
    public PagedModel<KitchenTicketResponse> getTickets(@PageableDefault(size = 20, sort = "receivedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<KitchenTicketResponse> page = kitchenTicketService.getTickets(pageable);
        return new PagedModel<>(page);
    }

    @GetMapping("/{ticketId}")
    public KitchenTicketResponse getSingleTicket(@PathVariable UUID ticketId) {
        return kitchenTicketService.getTicketById(ticketId);
    }
}
