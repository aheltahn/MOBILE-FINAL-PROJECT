package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.finalprojectmu.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderTrackingActivity extends BaseActivity {

    private TextView textViewOrderId, textProcessing, textShipping, textDelivered;
    // Thêm các TextView mới cho chi tiết đơn hàng
    private TextView tvOrderDate, tvOrderTotal, tvOrderAddress;
    
    private ImageView iconProcessing, iconShipping, iconDelivered;
    private View line1, line2;
    private Button buttonBackToHome;

    private FirebaseFirestore db;
    private ListenerRegistration orderListener;
    private String orderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        db = FirebaseFirestore.getInstance();

        initViews();

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewOrderId.setText(orderId.toUpperCase());

        buttonBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderTrackingActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        textViewOrderId = findViewById(R.id.textViewOrderId);
        textProcessing = findViewById(R.id.textProcessing);
        textShipping = findViewById(R.id.textShipping);
        textDelivered = findViewById(R.id.textDelivered);
        iconProcessing = findViewById(R.id.iconProcessing);
        iconShipping = findViewById(R.id.iconShipping);
        iconDelivered = findViewById(R.id.iconDelivered);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        buttonBackToHome = findViewById(R.id.buttonBackToHome);
        
        // Ánh xạ các view mới
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvOrderAddress = findViewById(R.id.tvOrderAddress);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (orderId != null) {
            DocumentReference orderRef = db.collection("orders").document(orderId);

            orderListener = orderRef.addSnapshotListener(this, (snapshot, e) -> {
                if (e != null) {
                    Toast.makeText(OrderTrackingActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String status = snapshot.getString("status");
                    if (status != null) {
                        updateStatusUI(status);
                    }
                    
                    // Cập nhật thông tin chi tiết đơn hàng
                    updateOrderDetails(snapshot);
                }
            });
        }
    }

    private void updateOrderDetails(com.google.firebase.firestore.DocumentSnapshot snapshot) {
        // Lấy ngày đặt
        if (snapshot.getDate("createdAt") != null) {
            Date date = snapshot.getDate("createdAt");
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
            tvOrderDate.setText("Ngày đặt: " + sdf.format(date));
        }

        // Lấy tổng tiền
        Double total = snapshot.getDouble("totalPrice");
        if (total != null) {
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvOrderTotal.setText("Tổng tiền: " + currencyFormatter.format(total));
        }

        // Lấy địa chỉ
        String address = snapshot.getString("shippingAddress");
        if (address != null) {
            tvOrderAddress.setText("Địa chỉ: " + address);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (orderListener != null) {
            orderListener.remove();
        }
    }

    private void updateStatusUI(String status) {
        resetStatusUI();

        switch (status) {
            case "Delivered":
                updateStep(textDelivered, iconDelivered, null, true);
            case "Shipping":
                updateStep(textShipping, iconShipping, line2, true);
            case "Pending":
                updateStep(textProcessing, iconProcessing, line1, true);
                break;
            case "Cancelled":
                textProcessing.setText("Đơn hàng đã bị hủy");
                textProcessing.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                break;
        }
    }

    private void resetStatusUI() {
        updateStep(textProcessing, iconProcessing, line1, false);
        updateStep(textShipping, iconShipping, line2, false);
        updateStep(textDelivered, iconDelivered, null, false);
        textProcessing.setText("Đang xử lý");
    }

    private void updateStep(TextView textView, ImageView imageView, View lineView, boolean isActive) {
        int activeColor = ContextCompat.getColor(this, R.color.theme_text_primary);
        int activeLineColor = ContextCompat.getColor(this, R.color.theme_accent_orange);
        int inactiveColor = ContextCompat.getColor(this, R.color.theme_text_secondary);

        if (isActive) {
            textView.setTextColor(activeColor);
            textView.setTypeface(null, Typeface.BOLD);
            if (lineView != null) {
                lineView.setBackgroundColor(activeLineColor);
            }
        } else {
            textView.setTextColor(inactiveColor);
            textView.setTypeface(null, Typeface.NORMAL);
            if (lineView != null) {
                lineView.setBackgroundColor(inactiveColor);
            }
        }
    }
}
