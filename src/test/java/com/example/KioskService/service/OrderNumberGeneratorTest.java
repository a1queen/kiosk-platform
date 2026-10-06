package com.example.KioskService.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class OrderNumberGeneratorTest {

    @Test
    @DisplayName("Should generate sequential order numbers starting with Z-001")
    void shouldGenerateSequentialOrderNumbers() {
        OrderNumberGenerator generator = new OrderNumberGenerator();

        assertThat(generator.generate()).isEqualTo("Z-001");
        assertThat(generator.generate()).isEqualTo("Z-002");
        assertThat(generator.generate()).isEqualTo("Z-003");
    }

    @Test
    @DisplayName("Should format order numbers with 3 or more digits")
    void shouldFormatWithThreeOrMoreDigits() throws Exception {
        OrderNumberGenerator generator = new OrderNumberGenerator();

        Field counterField = OrderNumberGenerator.class.getDeclaredField("counter");
        counterField.setAccessible(true);
        AtomicInteger counter = (AtomicInteger) counterField.get(generator);

        counter.set(9);
        assertThat(generator.generate()).isEqualTo("Z-010");

        counter.set(99);
        assertThat(generator.generate()).isEqualTo("Z-100");

        counter.set(999);
        assertThat(generator.generate()).isEqualTo("Z-1000");
    }

    @Test
    @DisplayName("Should reset counter to 1 on a new date")
    void shouldResetCounterOnNewDate() throws Exception {
        OrderNumberGenerator generator = new OrderNumberGenerator();

        // Generate some numbers
        generator.generate(); // Z-001
        generator.generate(); // Z-002

        // Simulate yesterday's date
        Field dateField = OrderNumberGenerator.class.getDeclaredField("lastResetDate");
        dateField.setAccessible(true);
        dateField.set(generator, LocalDate.now().minusDays(1));

        // When generating on "new" day (today)
        String firstOfNewDay = generator.generate();

        assertThat(firstOfNewDay).isEqualTo("Z-001");
        assertThat(dateField.get(generator)).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Should generate unique order numbers concurrently without race conditions")
    void shouldGenerateUniqueNumbersConcurrently() throws InterruptedException {
        OrderNumberGenerator generator = new OrderNumberGenerator();
        int threadCount = 20;
        int ordersPerThread = 50;
        int totalOrders = threadCount * ordersPerThread;

        Set<String> generatedNumbers = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < ordersPerThread; j++) {
                        generatedNumbers.add(generator.generate());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(generatedNumbers).hasSize(totalOrders);
    }
}
