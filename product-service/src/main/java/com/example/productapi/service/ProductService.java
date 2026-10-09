package com.example.productapi.service;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Used by the controller in the endpoint GET /api/products
     * @return The whole list of products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll(Sort.by("id"));
    }

    /**
     * Searches the product in the database by its ID
     * @param id ID of the product
     * @return The product with the matching ID
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Recieves a product from the controller and saves it, the database assigns the ID
     * @param product
     * @return The product with its assigned ID
     */
    public Product createProduct(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    /**
     * Search for the product using getProductbyId(id), if the product exists, overwrites the parameters
     * of the product with the new ones
     * @param id ID of the product to update
     * @param updatedProduct Updated parameters of the product
     * @return The updated product, empty if the product doesnt exist
     */
    @Transactional
    public Optional<Product> updateProduct(Long id, Product updatedProduct) {
        return getProductById(id).map(product -> {
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            return productRepository.save(product);
        });
    }

    /**
     * Deletes the especified product by ID
     * @param id ID of the product to delete
     * @return true if the product got deleted, false if it doesnt exist
     */
    @Transactional
    public boolean deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }
}
