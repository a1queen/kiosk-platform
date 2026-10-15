package com.example.kioskservice.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class OrderNumberGenerator {

    private static final String PREFIX = "Z-";
    private LocalDate lastResetDate = LocalDate.now();
    private int counter = 0;

    /**
     * Generates sequential order numbers in format Z-001, Z-002,
     * resetting the counter back to 1 at midnight every day.
     */
    public synchronized String generate() {
        LocalDate today = LocalDate.now();

        if (!today.isEqual(lastResetDate)) {
            lastResetDate = today;
            counter = 0;
        }

        counter++;
        return String.format("%s%03d", PREFIX, counter);
    }
}
