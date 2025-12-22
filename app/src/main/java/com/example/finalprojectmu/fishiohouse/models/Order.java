package com.example.finalprojectmu.fishiohouse.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Order {

    @Exclude
    private String orderId;

    private String userId;
    private double totalPrice;
    private String status;

    @ServerTimestamp
    private Date createdAt;

    public Order() {
    }

    public String getOrderId() {
        return null;
    }

    public String getUserId() {
        return null;
    }

    public double getTotalPrice() {
        return 0;
    }

    public String getStatus() {
        return null;
    }

    public Date getCreatedAt() {
        return null;
    }

    public void setOrderId(String orderId) {
    }
}
