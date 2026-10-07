package com.example.KioskService.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 128)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "notes", nullable = false, columnDefinition = "text[]")
    private List<String> notes = new ArrayList<>();

    public OrderItem(String productName, Integer quantity, List<String> notes) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name must not be blank");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        this.productName = productName;
        this.quantity = quantity;
        this.notes = notes != null ? new ArrayList<>(notes) : new ArrayList<>();
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public List<String> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem that)) return false;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return Objects.equals(productName, that.productName) &&
                Objects.equals(quantity, that.quantity) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return Objects.hash(productName, quantity, notes);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", notes=" + notes +
                '}';
    }
}
