package com.example.ecommerce.service;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_LOGGER");
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductService productService;
    
    private final Random random = new Random();
    
    public Order createOrder(String customerEmail, String customerName, Long productId, Integer quantity) {
        logger.info("Creating order for customer: {} - Product: {} - Quantity: {}", customerEmail, productId, quantity);
        
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (!productOpt.isPresent()) {
                logger.error("Product not found for order creation: {}", productId);
                errorLogger.error("{{\"event_type\":\"order_creation_failed\",\"reason\":\"product_not_found\",\"product_id\":{},\"customer_email\":\"{}\",\"timestamp\":\"{}\"}}",
                                productId, customerEmail, java.time.LocalDateTime.now());
                throw new RuntimeException("Product not found: " + productId);
            }
            
            Product product = productOpt.get();
            
            // Vérification du stock
            if (product.getStock() < quantity) {
                logger.warn("Insufficient stock for product {}: requested={}, available={}", productId, quantity, product.getStock());
                errorLogger.warn("{{\"event_type\":\"insufficient_stock\",\"product_id\":{},\"requested_quantity\":{},\"available_stock\":{},\"customer_email\":\"{}\",\"timestamp\":\"{}\"}}",
                               productId, quantity, product.getStock(), customerEmail, java.time.LocalDateTime.now());
                throw new RuntimeException("Insufficient stock");
            }
            
            Order order = new Order(customerEmail, customerName, product, quantity);
            Order savedOrder = orderRepository.save(order);
            
            // Mise à jour du stock
            productService.updateStock(productId, product.getStock() - quantity);
            
            businessLogger.info("{{\"event_type\":\"order_created\",\"order_id\":{},\"customer_email\":\"{}\",\"product_id\":{},\"quantity\":{},\"total_amount\":{},\"timestamp\":\"{}\"}}",
                              savedOrder.getId(), customerEmail, productId, quantity, savedOrder.getTotalAmount(), java.time.LocalDateTime.now());
            
            logger.info("Order created successfully: {}", savedOrder.getId());
            return savedOrder;
            
        } catch (Exception e) {
            logger.error("Error creating order for customer {}: {}", customerEmail, e.getMessage());
            errorLogger.error("{{\"event_type\":\"order_creation_error\",\"customer_email\":\"{}\",\"product_id\":{},\"error_message\":\"{}\",\"timestamp\":\"{}\"}}",
                            customerEmail, productId, e.getMessage(), java.time.LocalDateTime.now());
            throw e;
        }
    }
    
    public Order updateOrderStatus(Long orderId, String newStatus) {
        logger.info("Updating order {} status to: {}", orderId, newStatus);
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            logger.error("Order not found: {}", orderId);
            errorLogger.error("{{\"event_type\":\"order_not_found\",\"order_id\":{},\"timestamp\":\"{}\"}}",
                            orderId, java.time.LocalDateTime.now());
            throw new RuntimeException("Order not found: " + orderId);
        }
        
        Order order = orderOpt.get();
        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        businessLogger.info("{{\"event_type\":\"order_status_updated\",\"order_id\":{},\"old_status\":\"{}\",\"new_status\":\"{}\",\"customer_email\":\"{}\",\"timestamp\":\"{}\"}}",
                          orderId, oldStatus, newStatus, order.getCustomerEmail(), java.time.LocalDateTime.now());
        
        return updatedOrder;
    }
    
    public boolean processPayment(Long orderId, String paymentMethod) {
        logger.info("Processing payment for order {} with method: {}", orderId, paymentMethod);
        
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            logger.error("Order not found for payment: {}", orderId);
            return false;
        }
        
        Order order = orderOpt.get();
        
        // Simulation d'échec de paiement (10% de chance)
        if (random.nextInt(100) < 10) {
            logger.warn("Payment failed for order: {}", orderId);
            errorLogger.warn("{{\"event_type\":\"payment_failed\",\"order_id\":{},\"payment_method\":\"{}\",\"amount\":{},\"reason\":\"payment_declined\",\"timestamp\":\"{}\"}}",
                           orderId, paymentMethod, order.getTotalAmount(), java.time.LocalDateTime.now());
            return false;
        }
        
        order.setPaymentMethod(paymentMethod);
        order.setStatus("CONFIRMED");
        orderRepository.save(order);
        
        businessLogger.info("{{\"event_type\":\"payment_processed\",\"order_id\":{},\"payment_method\":\"{}\",\"amount\":{},\"customer_email\":\"{}\",\"timestamp\":\"{}\"}}",
                          orderId, paymentMethod, order.getTotalAmount(), order.getCustomerEmail(), java.time.LocalDateTime.now());
        
        logger.info("Payment processed successfully for order: {}", orderId);
        return true;
    }
    
    public List<Order> getOrdersByCustomer(String customerEmail) {
        logger.info("Fetching orders for customer: {}", customerEmail);
        List<Order> orders = orderRepository.findByCustomerEmail(customerEmail);
        businessLogger.info("{{\"event_type\":\"customer_orders_fetched\",\"customer_email\":\"{}\",\"orders_count\":{},\"timestamp\":\"{}\"}}",
                          customerEmail, orders.size(), java.time.LocalDateTime.now());
        return orders;
    }
    
    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        businessLogger.info("{{\"event_type\":\"all_orders_fetched\",\"total_orders\":{},\"timestamp\":\"{}\"}}",
                          orders.size(), java.time.LocalDateTime.now());
        return orders;
    }
    
    public BigDecimal calculateTotalRevenue() {
        logger.info("Calculating total revenue");
        List<Order> confirmedOrders = orderRepository.findByStatus("CONFIRMED");
        BigDecimal totalRevenue = confirmedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        businessLogger.info("{{\"event_type\":\"revenue_calculated\",\"total_revenue\":{},\"confirmed_orders_count\":{},\"timestamp\":\"{}\"}}",
                          totalRevenue, confirmedOrders.size(), java.time.LocalDateTime.now());
        
        return totalRevenue;
    }
}
