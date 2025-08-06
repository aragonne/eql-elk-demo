package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerEmail(String customerEmail);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN ?1 AND ?2")
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CONFIRMED'")
    Long countConfirmedOrders();
}
