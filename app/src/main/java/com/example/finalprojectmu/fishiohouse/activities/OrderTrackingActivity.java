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

// SỬA Ở ĐÂY: Có thể kế thừa từ BaseActivity nếu bạn muốn header chung
public class OrderTrackingActivity extends BaseActivity {

    private TextView textViewOrderId, textProcessing, textShipping, textDelivered;
    private ImageView iconProcessing, iconShipping, iconDelivered;
    private View line1, line2;
    private Button buttonBackToHome;

    private FirebaseFirestore db;
    private ListenerRegistration orderListener; // Biến để lắng nghe sự thay đổi
    private String orderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        db = FirebaseFirestore.getInstance();

        // 1. Ánh xạ các View từ layout
        initViews();

        // 2. Lấy orderId được gửi từ OrderHistoryActivity
        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có ID
            return;
        }

        // Hiển thị mã đơn hàng lên giao diện
        textViewOrderId.setText(orderId.toUpperCase());

        // 3. Thiết lập sự kiện cho nút "Quay về trang chủ"
        buttonBackToHome.setOnClickListener(v -> {
            // Có thể đổi thành finish() nếu chỉ muốn quay lại trang lịch sử
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
    }

    // 4. Bắt đầu lắng nghe khi Activity được hiển thị
    @Override
    protected void onStart() {
        super.onStart();
        if (orderId != null) {
            DocumentReference orderRef = db.collection("orders").document(orderId);

            // addSnapshotListener sẽ tự động chạy lại mỗi khi dữ liệu của đơn hàng này thay đổi
            orderListener = orderRef.addSnapshotListener(this, (snapshot, e) -> {
                if (e != null) {
                    Toast.makeText(OrderTrackingActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Lấy ra trạng thái (status) từ document
                    String status = snapshot.getString("status");
                    if (status != null) {
                        // Gọi hàm cập nhật giao diện với trạng thái mới nhất
                        updateStatusUI(status);
                    }
                }
            });
        }
    }

    // 5. Dừng lắng nghe khi Activity bị ẩn đi để tiết kiệm tài nguyên
    @Override
    protected void onStop() {
        super.onStop();
        if (orderListener != null) {
            orderListener.remove();
        }
    }

    // 6. Hàm quan trọng nhất: Cập nhật giao diện timeline dựa vào trạng thái
    private void updateStatusUI(String status) {
        // Reset tất cả về trạng thái chưa hoàn thành (màu xám)
        resetStatusUI();

        // Dùng switch-case để kích hoạt các bước tương ứng
        // Lưu ý: không có 'break' để các trạng thái sau kích hoạt các trạng thái trước đó
        switch (status) {
            case "Delivered":
                updateStep(textDelivered, iconDelivered, null, true);
            case "Shipping":
                updateStep(textShipping, iconShipping, line2, true);
            case "Pending":
                updateStep(textProcessing, iconProcessing, line1, true);
                break;
            case "Cancelled":
                // Xử lý riêng cho trạng thái "Đã hủy" (nếu có)
                textProcessing.setText("Đơn hàng đã bị hủy");
                textProcessing.setTextColor(ContextCompat.getColor(this, R.color.theme_accent_red)); // Giả sử bạn có màu đỏ
                break;
        }
    }

    // Hàm phụ: Đưa một bước về trạng thái ban đầu (chưa kích hoạt)
    private void resetStatusUI() {
        updateStep(textProcessing, iconProcessing, line1, false);
        updateStep(textShipping, iconShipping, line2, false);
        updateStep(textDelivered, iconDelivered, null, false);
        // Reset lại text nếu có trạng thái hủy
        textProcessing.setText("Đang xử lý");
    }

    // Hàm phụ: Cập nhật màu sắc, chữ đậm cho một bước
    private void updateStep(TextView textView, ImageView imageView, View lineView, boolean isActive) {
        // Màu cho trạng thái active (hoàn thành)
        int activeColor = ContextCompat.getColor(this, R.color.theme_text_primary);
        int activeLineColor = ContextCompat.getColor(this, R.color.theme_accent_orange);

        // Màu cho trạng thái inactive (chưa tới)
        int inactiveColor = ContextCompat.getColor(this, R.color.theme_text_secondary);

        if (isActive) {
            textView.setTextColor(activeColor);
            textView.setTypeface(null, Typeface.BOLD);
            // Bạn có thể đổi màu icon ở đây nếu muốn
            // Ví dụ: imageView.setColorFilter(activeLineColor);
            if (lineView != null) {
                lineView.setBackgroundColor(activeLineColor);
            }
        } else {
            textView.setTextColor(inactiveColor);
            textView.setTypeface(null, Typeface.NORMAL);
            // imageView.clearColorFilter();
            if (lineView != null) {
                lineView.setBackgroundColor(inactiveColor);
            }
        }
    }
}
