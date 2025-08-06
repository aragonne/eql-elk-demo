package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOGGER");
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Map<String, Object> orderRequest) {
        long startTime = System.currentTimeMillis();
        
        try {
            String customerEmail = (String) orderRequest.get("customerEmail");
            String customerName = (String) orderRequest.get("customerName");
            Long productId = Long.valueOf(orderRequest.get("productId").toString());
            Integer quantity = Integer.valueOf(orderRequest.get("quantity").toString());
            
            Order order = orderService.createOrder(customerEmail, customerName, productId, quantity);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("POST /api/orders - 201 - {}ms - Order created: {} for customer: {}", 
                            duration, order.getId(), customerEmail);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error creating order: {}", e.getMessage());
            accessLogger.error("POST /api/orders - 400 - {}ms - Error: {}", duration, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Order> orders = orderService.getAllOrders();
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("GET /api/orders - 200 - {}ms - {} orders returned", duration, orders.size());
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error fetching all orders: {}", e.getMessage());
            accessLogger.error("GET /api/orders - 500 - {}ms - Error: {}", duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String email) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Order> orders = orderService.getOrdersByCustomer(email);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("GET /api/orders/customer/{} - 200 - {}ms - {} orders found", 
                            email, duration, orders.size());
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error fetching orders for customer {}: {}", email, e.getMessage());
            accessLogger.error("GET /api/orders/customer/{} - 500 - {}ms - Error: {}", 
                             email, duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        long startTime = System.currentTimeMillis();
        
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("PUT /api/orders/{}/status - 200 - {}ms - Status updated to: {}", 
                            id, duration, status);
            return ResponseEntity.ok(updatedOrder);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error updating order {} status: {}", id, e.getMessage());
            accessLogger.error("PUT /api/orders/{}/status - 400 - {}ms - Error: {}", 
                             id, duration, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/payment")
    public ResponseEntity<Map<String, Object>> processPayment(@PathVariable Long id, @RequestBody Map<String, String> paymentRequest) {
        long startTime = System.currentTimeMillis();
        
        try {
            String paymentMethod = paymentRequest.get("paymentMethod");
            boolean success = orderService.processPayment(id, paymentMethod);
            long duration = System.currentTimeMillis() - startTime;
            
            if (success) {
                accessLogger.info("POST /api/orders/{}/payment - 200 - {}ms - Payment successful with {}", 
                                id, duration, paymentMethod);
                return ResponseEntity.ok(Map.of("success", true, "message", "Payment processed successfully"));
            } else {
                accessLogger.warn("POST /api/orders/{}/payment - 400 - {}ms - Payment failed with {}", 
                                id, duration, paymentMethod);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Payment failed"));
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error processing payment for order {}: {}", id, e.getMessage());
            accessLogger.error("POST /api/orders/{}/payment - 500 - {}ms - Error: {}", 
                             id, duration, e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, BigDecimal>> getTotalRevenue() {
        long startTime = System.currentTimeMillis();
        
        try {
            BigDecimal totalRevenue = orderService.calculateTotalRevenue();
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("GET /api/orders/revenue - 200 - {}ms - Total revenue: {}", 
                            duration, totalRevenue);
            return ResponseEntity.ok(Map.of("totalRevenue", totalRevenue));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error calculating total revenue: {}", e.getMessage());
            accessLogger.error("GET /api/orders/revenue - 500 - {}ms - Error: {}", duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
