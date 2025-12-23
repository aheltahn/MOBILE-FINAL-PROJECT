package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;

public class Food {
    @Exclude
    private String id; // ID này sẽ không được lưu vào Firestore, chỉ dùng trong app

    private String name;
    private double price;
    private String description;
    private String imageUrl;

    // ================== THAY ĐỔI QUAN TRỌNG ==================
    // Đổi tên 'type' thành 'category' để khớp với tên trường trên Firebase
    private String category;
    // =======================================================

    // Bắt buộc phải có một constructor rỗng để Firestore có thể tự động chuyển đổi document thành đối tượng Food
    public Food() {}

    // Constructor đầy đủ (có thể dùng khi bạn tạo món ăn mới)
    public Food(String name, double price, String description, String imageUrl, String category) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category; // Sửa ở đây
    }

    // --- Getters and Setters ---
    // Các hàm getter/setter cho các thuộc tính khác được giữ nguyên

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

    // ================== SỬA GETTER VÀ SETTER CHO PHÙ HỢP ==================
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    // =====================================================================
}
