package com.example.finalprojectmu.fishiohouse.data;

public class ProductEntity {
    private String id;
    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private String type;

    public ProductEntity() {
    }

    public ProductEntity(String id, String name, double price, String description, String imageUrl, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}