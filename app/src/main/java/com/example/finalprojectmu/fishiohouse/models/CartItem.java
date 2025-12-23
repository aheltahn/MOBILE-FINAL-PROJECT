package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;
import java.io.Serializable; // <<===== 1. THÊM DÒNG NÀY

//                                               vvvvvvvvvvvvvvvvvvvvvvvvv
public class CartItem implements Serializable { // <<===== 2. THÊM VÀO ĐÂY
    @Exclude
    private String foodId;

    private String name;
    private double price;
    private long quantity;
    private String imageUrl;

    @Exclude // Không lưu trường này vào Firestore
    private boolean isSelected = true; // Mặc định là được chọn

    public CartItem() {}

    // Getters and Setters (Giữ nguyên không đổi)
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    @Exclude
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
