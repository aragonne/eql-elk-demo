package com.example.ecommerce.service;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_LOGGER");
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        businessLogger.info("{{\"event_type\":\"product_list\",\"count\":{},\"timestamp\":\"{}\"}}", 
                          products.size(), java.time.LocalDateTime.now());
        return products;
    }
    
    public Optional<Product> getProductById(Long id) {
        logger.info("Fetching product with id: {}", id);
        Optional<Product> product = productRepository.findById(id);
        
        if (product.isPresent()) {
            businessLogger.info("{{\"event_type\":\"product_view\",\"product_id\":{},\"product_name\":\"{}\",\"category\":\"{}\",\"timestamp\":\"{}\"}}",
                              id, product.get().getName(), product.get().getCategory(), java.time.LocalDateTime.now());
        } else {
            logger.warn("Product not found with id: {}", id);
            businessLogger.warn("{{\"event_type\":\"product_not_found\",\"product_id\":{},\"timestamp\":\"{}\"}}",
                               id, java.time.LocalDateTime.now());
        }
        
        return product;
    }
    
    public List<Product> getProductsByCategory(String category) {
        logger.info("Fetching products by category: {}", category);
        List<Product> products = productRepository.findByCategory(category);
        businessLogger.info("{{\"event_type\":\"category_search\",\"category\":\"{}\",\"count\":{},\"timestamp\":\"{}\"}}",
                          category, products.size(), java.time.LocalDateTime.now());
        return products;
    }
    
    public List<Product> searchProducts(String query) {
        logger.info("Searching products with query: {}", query);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(query);
        businessLogger.info("{{\"event_type\":\"product_search\",\"query\":\"{}\",\"results_count\":{},\"timestamp\":\"{}\"}}",
                          query, products.size(), java.time.LocalDateTime.now());
        return products;
    }
    
    public Product saveProduct(Product product) {
        logger.info("Saving new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        businessLogger.info("{{\"event_type\":\"product_created\",\"product_id\":{},\"product_name\":\"{}\",\"category\":\"{}\",\"price\":{},\"timestamp\":\"{}\"}}",
                          savedProduct.getId(), savedProduct.getName(), savedProduct.getCategory(), 
                          savedProduct.getPrice(), java.time.LocalDateTime.now());
        return savedProduct;
    }
    
    public boolean updateStock(Long productId, Integer newStock) {
        logger.info("Updating stock for product {}: new stock = {}", productId, newStock);
        Optional<Product> productOpt = productRepository.findById(productId);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            Integer oldStock = product.getStock();
            product.setStock(newStock);
            productRepository.save(product);
            
            businessLogger.info("{{\"event_type\":\"stock_update\",\"product_id\":{},\"old_stock\":{},\"new_stock\":{},\"timestamp\":\"{}\"}}",
                              productId, oldStock, newStock, java.time.LocalDateTime.now());
            return true;
        } else {
            logger.error("Cannot update stock: Product not found with id: {}", productId);
            return false;
        }
    }
}
