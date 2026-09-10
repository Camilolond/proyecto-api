package com.example.productapi.service;

import com.example.productapi.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public ProductService() {
        products.add(new Product(counter.incrementAndGet(), "Laptop", "High-performance laptop", 1200.00, 10));
        products.add(new Product(counter.incrementAndGet(), "Mouse", "Wireless ergonomic mouse", 25.50, 50));
        products.add(new Product(counter.incrementAndGet(), "Keyboard", "Mechanical RGB keyboard", 89.99, 30));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Optional<Product> getProductById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    public Product createProduct(Product product) {
        product.setId(counter.incrementAndGet());
        products.add(product);
        return product;
    }

    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return getProductById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            return product;
        });
    }

    public boolean deleteProduct(Long id) {
        return products.removeIf(product -> product.getId().equals(id));
    }
}
