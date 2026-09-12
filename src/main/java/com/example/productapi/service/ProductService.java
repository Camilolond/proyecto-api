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

    /**
     * Constructor method, preloads the list with 3 example products, each with an ID from 1 to 3
     */
    public ProductService() {
        products.add(new Product(counter.incrementAndGet(), "Laptop", "High-performance laptop", 1200.00, 10));
        products.add(new Product(counter.incrementAndGet(), "Mouse", "Wireless ergonomic mouse", 25.50, 50));
        products.add(new Product(counter.incrementAndGet(), "Keyboard", "Mechanical RGB keyboard", 89.99, 30));
    }

    /**
     * Used by the controller in the endpoint GET /api/products
     * @return The whole list of products
     */
    public List<Product> getAllProducts() {
        return products;
    }

    /**
     * Iterates over the list using a stream and filters the product whose ID matches the requested ID
     * @param id ID of the product
     * @return The first product with the matching ID
     */
    public Optional<Product> getProductById(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst();
    }

    /**
     * Recieves a product from the controller and adds it to the list as well as asigning an ID to it
     * @param product
     * @return The product with its assigned ID
     */
    public Product createProduct(Product product) {
        product.setId(counter.incrementAndGet());
        products.add(product);
        return product;
    }

    /**
     * Search for the product using getProductbyId(id), if the product exists, overwrites the parameters
     * of the product with the new ones
     * @param id ID of the product to update
     * @param updatedProduct Updated parameters of the product
     * @return 200 Ok if the product its updated, 404 Not found if the product doesnt exist
     */
    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return getProductById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            return product;
        });
    }

    /**
     * Deletes the especified product by ID
     * @param id ID of the product to delete
     * @return 204 if the product its deleted
     */
    public boolean deleteProduct(Long id) {
        return products.removeIf(product -> product.getId().equals(id));
    }
}
