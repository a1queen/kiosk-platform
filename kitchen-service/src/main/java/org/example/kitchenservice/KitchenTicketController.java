package org.example.kitchenservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class KitchenTicketController {
    private final KitchenTicketService kitchenTicketService;

    @PatchMapping
    public ResponseEntity<KitchenTicketResponse> changeStatusToPreparing(@PathVariable UUID ticketId) {
        KitchenTicketResponse response = kitchenTicketService.markAsPreparing(ticketId);
        return ResponseEntity.ok(response);
    }

}
