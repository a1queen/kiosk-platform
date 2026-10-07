package com.example.KioskService.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestedOrderTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should create RequestedOrder and access all record components")
    void shouldCreateRequestedOrderAndAccessComponents() {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 2, List.of("No pickle"));
        RequestedOrder requestedOrder = new RequestedOrder(new BigDecimal("25.50"), List.of(item));

        assertThat(requestedOrder.totalAmount()).isEqualTo(new BigDecimal("25.50"));
        assertThat(requestedOrder.items()).containsExactly(item);
        assertThat(item.productName()).isEqualTo("Burger");
        assertThat(item.quantity()).isEqualTo(2);
        assertThat(item.notes()).containsExactly("No pickle");
    }

    @Test
    @DisplayName("Should verify equals, hashCode, and toString for RequestedOrder and Item")
    void shouldVerifyEqualsHashCodeAndToString() {
        RequestedOrder.Item item1 = new RequestedOrder.Item("Burger", 2, List.of("No pickle"));
        RequestedOrder.Item item2 = new RequestedOrder.Item("Burger", 2, List.of("No pickle"));
        RequestedOrder.Item differentItem = new RequestedOrder.Item("Fries", 1, null);

        RequestedOrder order1 = new RequestedOrder(new BigDecimal("25.50"), List.of(item1));
        RequestedOrder order2 = new RequestedOrder(new BigDecimal("25.50"), List.of(item2));
        RequestedOrder differentOrder = new RequestedOrder(new BigDecimal("10.00"), List.of(differentItem));

        assertThat(item1).isEqualTo(item2)
                .hasSameHashCodeAs(item2)
                .isNotEqualTo(differentItem);
        assertThat(item1.toString()).contains("Burger", "2", "No pickle");

        assertThat(order1).isEqualTo(order2)
                .hasSameHashCodeAs(order2)
                .isNotEqualTo(differentOrder);
        assertThat(order1.toString()).contains("25.50");
    }

    @Test
    @DisplayName("Valid RequestedOrder should have no validation violations")
    void shouldHaveNoViolationsForValidRequest() {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 1, List.of("Extra cheese"));
        RequestedOrder order = new RequestedOrder(new BigDecimal("15.99"), List.of(item));

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should violate constraint when totalAmount is null")
    void shouldViolateWhenTotalAmountIsNull() {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 1, null);
        RequestedOrder order = new RequestedOrder(null, List.of(item));

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Total amount is required");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-0.01", "-15.00"})
    @DisplayName("Should violate constraint when totalAmount is less than 0.01")
    void shouldViolateWhenTotalAmountIsLessThanMinimum(String amount) {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", 1, null);
        RequestedOrder order = new RequestedOrder(new BigDecimal(amount), List.of(item));

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should violate constraint when items list is null")
    void shouldViolateWhenItemsListIsNull() {
        RequestedOrder order = new RequestedOrder(new BigDecimal("10.00"), null);

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should violate constraint when items list is empty")
    void shouldViolateWhenItemsListIsEmpty() {
        RequestedOrder order = new RequestedOrder(new BigDecimal("10.00"), Collections.emptyList());

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Order must contain at least one item");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t", "\n"})
    @DisplayName("Should violate constraint when item productName is blank or null")
    void shouldViolateWhenItemProductNameIsBlank(String productName) {
        RequestedOrder.Item item = new RequestedOrder.Item(productName, 1, null);
        RequestedOrder order = new RequestedOrder(new BigDecimal("10.00"), List.of(item));

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Product name must not be blank");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Should violate constraint when item quantity is not positive")
    void shouldViolateWhenItemQuantityIsNotPositive(int quantity) {
        RequestedOrder.Item item = new RequestedOrder.Item("Burger", quantity, null);
        RequestedOrder order = new RequestedOrder(new BigDecimal("10.00"), List.of(item));

        Set<ConstraintViolation<RequestedOrder>> violations = validator.validate(order);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("Quantity must be greater than zero");
    }
}
