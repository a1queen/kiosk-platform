package com.example.kioskservice.model;

import com.example.kioskservice.exception.DomainValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            throw new DomainValidationException("Product name must not be blank");
        }
        if (quantity == null || quantity <= 0) {
            throw new DomainValidationException("Quantity must be greater than zero");
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
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        OrderItem other = (OrderItem) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
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
