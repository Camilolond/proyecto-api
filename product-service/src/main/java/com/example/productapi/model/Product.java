package com.example.productapi.model;

public class Product {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;

    public Product() {
    }

    /**
     * Product constructor method
     * @param id ID of the product, not specified by the user
     * @param name Name of the product
     * @param description Description of the product
     * @param price Price of the product
     * @param stock Stock of the product
     */
    public Product(Long id, String name, String description, Double price, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}