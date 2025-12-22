package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;

public class Food {
    @Exclude
    private String id;

    private String name;
    private double price;
    private String description;
    private String imageUrl;
    private String type;

    public Food() {}

    public Food(String name, double price, String description, String imageUrl, String type) {}

    public Food(String name, double price, String description, String imageUrl) {}

    public String getId() { return null; }
    public void setId(String id) { }

    public String getName() { return null; }
    public void setName(String name) { }

    public double getPrice() { return 0; }
    public void setPrice(double price) { }

    public String getDescription() { return null; }
    public void setDescription(String description) { }

    public String getImageUrl() { return null; }
    public void setImageUrl(String imageUrl) { }

    public String getType() { return null; }
    public void setType(String type) { }
}
