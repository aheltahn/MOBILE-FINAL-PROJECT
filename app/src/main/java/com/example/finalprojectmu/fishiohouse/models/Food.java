package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable;

public class Food implements java.io.Serializable {
    @Exclude
    private String id;

    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private String type; // Thêm trường loại món ăn (VD: sushi, soup, drink)

    public Food() {}

    public Food(String name, double price, String description, String imageUrl, String type) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.type = type;
    }

    // Constructor cũ để tương thích code cũ (mặc định type là "other")
    public Food(String name, double price, String description, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.type = "other";
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
