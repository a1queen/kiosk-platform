package com.example.KioskService.repository;

import com.example.KioskService.model.Order;
import com.example.KioskService.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(OrderStatus status);

    @Query(
            value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items",
            countQuery = "SELECT count(o) FROM Order o"
    )
    Page<Order> findAllWithItems(Pageable pageable);
}
