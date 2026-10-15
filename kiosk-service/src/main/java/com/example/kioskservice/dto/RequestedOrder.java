package com.example.kioskservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record RequestedOrder(
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Total amount has invalid format (maximum 8 integer digits and 2 decimals)")
        BigDecimal totalAmount,

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid Item> items
) {
    public RequestedOrder {
        items = items != null ? List.copyOf(items) : List.of();
    }

    public record Item(
            @NotBlank(message = "Product name must not be blank")
            @Size(max = 128, message = "Product name must not exceed 128 characters")
            String productName,

            @NotNull(message = "Quantity is required")
            @Positive(message = "Quantity must be greater than zero")
            Integer quantity,

            List<String> notes
    ) {
        public Item {
            notes = notes != null ? List.copyOf(notes) : List.of();
        }
    }
}
