package com.example.KioskService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record RequestedOrder(
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
        BigDecimal totalAmount,

        @NotEmpty(message = "Order must contain at least one item")
        @JsonProperty("items")
        List<@Valid Item> items
) {
    public record Item(
            @NotBlank(message = "Product name must not be blank")
            String productName,

            @Positive(message = "Quantity must be greater than zero")
            int quantity,

            List<String> notes
    ) {
        public Item {
            notes = notes != null ? List.copyOf(notes) : List.of();
        }
    }
}
