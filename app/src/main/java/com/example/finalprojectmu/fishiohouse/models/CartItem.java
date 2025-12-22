package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;

public class CartItem {
    @Exclude
    private String foodId;

    private String name;
    private double price;
    private long quantity;
    private String imageUrl;

    @Exclude
    private boolean isSelected = true;

    public CartItem() {}

    public String getFoodId() { return null; }

    public void setFoodId(String foodId) {}

    public String getName() { return null; }

    public void setName(String name) {}

    public double getPrice() { return 0; }

    public void setPrice(double price) {}

    public long getQuantity() { return 0; }

    public void setQuantity(long quantity) {}

    public String getImageUrl() { return null; }

    public void setImageUrl(String imageUrl) {}

    @Exclude
    public boolean isSelected() { return false; }

    @Exclude
    public void setSelected(boolean selected) {}
}
