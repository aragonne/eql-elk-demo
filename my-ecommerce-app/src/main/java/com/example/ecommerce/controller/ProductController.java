package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOGGER");
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Product> products = productService.getAllProducts();
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("GET /api/products - 200 - {}ms - {} products returned", duration, products.size());
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error fetching all products: {}", e.getMessage());
            accessLogger.error("GET /api/products - 500 - {}ms - Error: {}", duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<Product> product = productService.getProductById(id);
            long duration = System.currentTimeMillis() - startTime;
            
            if (product.isPresent()) {
                accessLogger.info("GET /api/products/{} - 200 - {}ms - Product found", id, duration);
                return ResponseEntity.ok(product.get());
            } else {
                accessLogger.warn("GET /api/products/{} - 404 - {}ms - Product not found", id, duration);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error fetching product {}: {}", id, e.getMessage());
            accessLogger.error("GET /api/products/{} - 500 - {}ms - Error: {}", id, duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Product> products = productService.getProductsByCategory(category);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("GET /api/products/category/{} - 200 - {}ms - {} products found", 
                            category, duration, products.size());
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error fetching products by category {}: {}", category, e.getMessage());
            accessLogger.error("GET /api/products/category/{} - 500 - {}ms - Error: {}", 
                             category, duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Product> products = productService.searchProducts(q);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("GET /api/products/search?q={} - 200 - {}ms - {} results", 
                            q, duration, products.size());
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error searching products with query '{}': {}", q, e.getMessage());
            accessLogger.error("GET /api/products/search?q={} - 500 - {}ms - Error: {}", 
                             q, duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        long startTime = System.currentTimeMillis();
        
        try {
            Product savedProduct = productService.saveProduct(product);
            long duration = System.currentTimeMillis() - startTime;
            
            accessLogger.info("POST /api/products - 201 - {}ms - Product created: {}", 
                            duration, savedProduct.getId());
            return ResponseEntity.ok(savedProduct);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error creating product: {}", e.getMessage());
            accessLogger.error("POST /api/products - 500 - {}ms - Error: {}", duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PutMapping("/{id}/stock")
    public ResponseEntity<Void> updateStock(@PathVariable Long id, @RequestParam Integer stock) {
        long startTime = System.currentTimeMillis();
        
        try {
            boolean updated = productService.updateStock(id, stock);
            long duration = System.currentTimeMillis() - startTime;
            
            if (updated) {
                accessLogger.info("PUT /api/products/{}/stock - 200 - {}ms - Stock updated to {}", 
                                id, duration, stock);
                return ResponseEntity.ok().build();
            } else {
                accessLogger.warn("PUT /api/products/{}/stock - 404 - {}ms - Product not found", id, duration);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error updating stock for product {}: {}", id, e.getMessage());
            accessLogger.error("PUT /api/products/{}/stock - 500 - {}ms - Error: {}", 
                             id, duration, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
