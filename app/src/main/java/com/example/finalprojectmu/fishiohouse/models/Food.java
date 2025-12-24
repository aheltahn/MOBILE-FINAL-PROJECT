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
    private String category; // Trường này tương đương với 'type'

    public Food() {}

    public Food(String name, double price, String description, String imageUrl, String category) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
    }

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

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // Thêm phương thức getType để tương thích với code cũ
    // Nó sẽ trả về giá trị của category
    public String getType() {
        return category;
    }

    // Thêm setType luôn cho chắc chắn
    public void setType(String type) {
        this.category = type;
    }
}
