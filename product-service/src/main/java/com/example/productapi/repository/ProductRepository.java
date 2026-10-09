package com.example.productapi.repository;

import com.example.productapi.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Access to the products table, Spring Data generates the implementation
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
