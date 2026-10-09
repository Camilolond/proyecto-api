package com.example.productapi.config;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataLoader(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Preloads 3 example products the first time the app starts with an empty database
     */
    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(List.of(
                new Product(null, "Laptop", "High-performance laptop", 1200.00, 10),
                new Product(null, "Mouse", "Wireless ergonomic mouse", 25.50, 50),
                new Product(null, "Keyboard", "Mechanical RGB keyboard", 89.99, 30)));
    }
}
