package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Order {

    @Exclude
    private String orderId; // Dùng @Exclude để Firebase không cố gắng lưu trường này

    private String userId;
    private double totalPrice;
    private String status;

    @ServerTimestamp
    private Date createdAt; // Firebase sẽ tự điền thời gian server vào đây

    // *** Constructor rỗng BẮT BUỘC phải có cho Firebase ***
    public Order() {
    }

    // Constructor để bạn dễ dàng tạo đối tượng trong code
    public Order(String userId, double totalPrice, String status) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.status = status;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
