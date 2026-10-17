package com.example.kioskservice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTest {

    @Test
    @DisplayName("Should contain all expected order statuses")
    void shouldContainExpectedStatuses() {
        OrderStatus[] statuses = OrderStatus.values();

        assertThat(statuses).containsExactly(
                OrderStatus.CREATED,
                OrderStatus.COMPLETED
        );
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    @DisplayName("Should correctly find status by valueOf")
    void shouldFindStatusByValueOf(OrderStatus status) {
        OrderStatus found = OrderStatus.valueOf(status.name());

        assertThat(found).isSameAs(status);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when valueOf name does not exist")
    void shouldThrowExceptionForInvalidStatusName() {
        assertThatThrownBy(() -> OrderStatus.valueOf("INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
