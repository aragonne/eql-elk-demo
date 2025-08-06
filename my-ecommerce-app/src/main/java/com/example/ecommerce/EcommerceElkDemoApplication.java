package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcommerceElkDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceElkDemoApplication.class, args);
    }
}
