package com.example.productapi.controller;

import com.example.productapi.model.Product;
import com.example.productapi.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Responds to a GET /api/products call
     * @return Complete list of products as JSON
     */
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * Responds to a GET /api/products/{id} call
     * @param id ID of the product
     * @return Specified product with a 200 OK status, 404 Not Found if the id doesnt exists
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Responds to a POST /api/products call
     * @param product JSON body of the object
     * @return Created product with a 201 status
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    /**
     * Responds to a PUT /api/products/{id}
     * @param id ID of the product to update
     * @param product Updated JSON body of the object
     * @return 200 OK Status with the updated product, if the product doesnt exist returns 404 Not found instead
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Responds to a DELETE /api/product/{id}
     * @param id ID of the product to delete
     * @return 204 No content if the product got deleted, 404 Not found if the product doesnt exists
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}