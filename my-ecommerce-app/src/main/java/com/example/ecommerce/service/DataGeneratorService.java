package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class DataGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataGeneratorService.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_LOGGER");
    private static final Logger errorLogger = LoggerFactory.getLogger("ERROR_LOGGER");
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    private final Faker faker = new Faker();
    private final Random random = new Random();
    
    private final List<String> categories = Arrays.asList(
        "Electronics", "Clothing", "Books", "Home & Garden", "Sports", "Toys", "Beauty", "Automotive"
    );
    
    private final List<String> paymentMethods = Arrays.asList(
        "CREDIT_CARD", "PAYPAL", "BANK_TRANSFER", "APPLE_PAY", "GOOGLE_PAY"
    );
    
    public void initializeProducts() {
        logger.info("Initializing sample products...");
        
        // Création de produits d'exemple pour chaque catégorie
        for (String category : categories) {
            for (int i = 0; i < 5; i++) {
                Product product = new Product(
                    generateProductName(category),
                    BigDecimal.valueOf(faker.number().randomDouble(2, 10, 500)),
                    category,
                    faker.number().numberBetween(0, 100)
                );
                productService.saveProduct(product);
            }
        }
        
        businessLogger.info("{{\"event_type\":\"data_initialization\",\"products_created\":{},\"categories\":{},\"timestamp\":\"{}\"}}",
                          categories.size() * 5, categories.size(), java.time.LocalDateTime.now());
        
        logger.info("Sample products initialized successfully");
    }
    
    public void simulateUserTraffic(int requestCount) {
        logger.info("Simulating {} user requests...", requestCount);
        
        for (int i = 0; i < requestCount; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    simulateRandomUserAction();
                    Thread.sleep(random.nextInt(1000) + 100); // Délai aléatoire entre 100ms et 1.1s
                } catch (Exception e) {
                    logger.error("Error in traffic simulation: {}", e.getMessage());
                }
            });
        }
        
        businessLogger.info("{{\"event_type\":\"traffic_simulation_started\",\"request_count\":{},\"timestamp\":\"{}\"}}",
                          requestCount, java.time.LocalDateTime.now());
    }
    
    private void simulateRandomUserAction() {
        String[] actions = {"browse_products", "search", "view_product", "create_order", "browse_category"};
        String action = actions[random.nextInt(actions.length)];
        
        try {
            switch (action) {
                case "browse_products":
                    productService.getAllProducts();
                    break;
                    
                case "search":
                    String searchTerm = faker.commerce().productName().split(" ")[0];
                    productService.searchProducts(searchTerm);
                    break;
                    
                case "view_product":
                    Long productId = (long) faker.number().numberBetween(1, 40);
                    productService.getProductById(productId);
                    break;
                    
                case "create_order":
                    simulateOrderCreation();
                    break;
                    
                case "browse_category":
                    String category = categories.get(random.nextInt(categories.size()));
                    productService.getProductsByCategory(category);
                    break;
            }
            
            businessLogger.info("{{\"event_type\":\"user_action_simulated\",\"action\":\"{}\",\"user_ip\":\"{}\",\"timestamp\":\"{}\"}}",
                              action, faker.internet().ipV4Address(), java.time.LocalDateTime.now());
                              
        } catch (Exception e) {
            errorLogger.error("{{\"event_type\":\"simulation_error\",\"action\":\"{}\",\"error\":\"{}\",\"timestamp\":\"{}\"}}",
                            action, e.getMessage(), java.time.LocalDateTime.now());
        }
    }
    
    private void simulateOrderCreation() {
        try {
            String customerEmail = faker.internet().emailAddress();
            String customerName = faker.name().fullName();
            Long productId = (long) faker.number().numberBetween(1, 40);
            Integer quantity = faker.number().numberBetween(1, 5);
            
            // 80% de chance de succès de commande
            if (random.nextInt(100) < 80) {
                orderService.createOrder(customerEmail, customerName, productId, quantity);
                
                // 70% de chance de procéder au paiement
                if (random.nextInt(100) < 70) {
                    // Simuler un délai de traitement
                    Thread.sleep(random.nextInt(2000) + 500);
                    
                    String paymentMethod = paymentMethods.get(random.nextInt(paymentMethods.size()));
                    orderService.processPayment(productId, paymentMethod);
                }
            }
            
        } catch (Exception e) {
            // Les erreurs sont déjà loggées par OrderService
            logger.debug("Order simulation resulted in expected error: {}", e.getMessage());
        }
    }
    
    public void simulateErrors(int errorCount) {
        logger.info("Simulating {} errors...", errorCount);
        
        for (int i = 0; i < errorCount; i++) {
            CompletableFuture.runAsync(() -> {
                try {
                    simulateRandomError();
                    Thread.sleep(random.nextInt(500) + 100);
                } catch (Exception e) {
                    logger.error("Error in error simulation: {}", e.getMessage());
                }
            });
        }
        
        businessLogger.info("{{\"event_type\":\"error_simulation_started\",\"error_count\":{},\"timestamp\":\"{}\"}}",
                          errorCount, java.time.LocalDateTime.now());
    }
    
    private void simulateRandomError() {
        String[] errorTypes = {"database_timeout", "network_error", "validation_error", "payment_gateway_error", "out_of_memory"};
        String errorType = errorTypes[random.nextInt(errorTypes.length)];
        
        switch (errorType) {
            case "database_timeout":
                errorLogger.error("{{\"event_type\":\"database_error\",\"error_type\":\"timeout\",\"query\":\"SELECT * FROM products\",\"duration_ms\":{},\"timestamp\":\"{}\"}}",
                                faker.number().numberBetween(5000, 30000), java.time.LocalDateTime.now());
                logger.error("Database timeout occurred during product query");
                break;
                
            case "network_error":
                errorLogger.error("{{\"event_type\":\"network_error\",\"error_type\":\"connection_refused\",\"service\":\"payment-gateway\",\"retry_count\":{},\"timestamp\":\"{}\"}}",
                                faker.number().numberBetween(1, 5), java.time.LocalDateTime.now());
                logger.error("Network connection refused to payment gateway");
                break;
                
            case "validation_error":
                errorLogger.warn("{{\"event_type\":\"validation_error\",\"field\":\"email\",\"value\":\"invalid-email\",\"user_ip\":\"{}\",\"timestamp\":\"{}\"}}",
                               faker.internet().ipV4Address(), java.time.LocalDateTime.now());
                logger.warn("Validation error: invalid email format");
                break;
                
            case "payment_gateway_error":
                errorLogger.error("{{\"event_type\":\"payment_error\",\"gateway\":\"stripe\",\"error_code\":\"card_declined\",\"amount\":{},\"timestamp\":\"{}\"}}",
                                faker.number().randomDouble(2, 10, 1000), java.time.LocalDateTime.now());
                logger.error("Payment gateway error: card declined");
                break;
                
            case "out_of_memory":
                errorLogger.error("{{\"event_type\":\"system_error\",\"error_type\":\"out_of_memory\",\"heap_size\":\"{}MB\",\"used_memory\":\"{}MB\",\"timestamp\":\"{}\"}}",
                                faker.number().numberBetween(512, 2048), faker.number().numberBetween(400, 1800), java.time.LocalDateTime.now());
                logger.error("OutOfMemoryError: Java heap space");
                break;
        }
    }
    
    private String generateProductName(String category) {
        switch (category) {
            case "Electronics":
                return faker.options().option("Smartphone", "Laptop", "Tablet", "Headphones", "Camera") + " " + faker.company().name();
            case "Clothing":
                return faker.options().option("T-Shirt", "Jeans", "Dress", "Jacket", "Shoes") + " " + faker.color().name();
            case "Books":
                return "The " + faker.book().title();
            case "Home & Garden":
                return faker.options().option("Sofa", "Table", "Chair", "Lamp", "Plant") + " " + faker.color().name();
            case "Sports":
                return faker.options().option("Running", "Tennis", "Football", "Basketball", "Yoga") + " " + faker.options().option("Shoes", "Ball", "Equipment");
            case "Toys":
                return faker.options().option("LEGO", "Doll", "Car", "Puzzle", "Game") + " " + faker.color().name();
            case "Beauty":
                return faker.options().option("Lipstick", "Foundation", "Perfume", "Shampoo", "Cream") + " " + faker.company().name();
            case "Automotive":
                return faker.options().option("Tire", "Battery", "Oil", "Filter", "Light") + " for " + faker.options().option("BMW", "Toyota", "Ford");
            default:
                return faker.commerce().productName();
        }
    }
}
