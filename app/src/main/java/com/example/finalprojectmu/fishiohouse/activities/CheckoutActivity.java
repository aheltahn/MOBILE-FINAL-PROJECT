package com.example.finalprojectmu.fishiohouse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.CartItem;
import com.example.finalprojectmu.fishiohouse.models.Voucher; // <<< THÊM IMPORT
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class CheckoutActivity extends AppCompatActivity {

    // === VIEWS ===
    private TextInputEditText edtName, edtPhone, edtAddress;
    private TextView txtSubtotal, txtShippingFee, txtTotalAmount, txtDiscount, txtSelectVoucherLabel;
    private Button btnConfirmOrder;
    private RelativeLayout layoutDiscount, layoutSelectVoucher;

    // === DATA ===
    private double subtotal = 0;
    private final double shippingFee = 15000;
    private Voucher appliedVoucher = null; // <<< THAY ĐỔI: Lưu cả đối tượng Voucher

    // === FIREBASE ===
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<CartItem> selectedItems;
    private String uid;

    // === CẬP NHẬT: Launcher để nhận kết quả từ VoucherListActivity ===
    private final ActivityResultLauncher<Intent> voucherLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.hasExtra("SELECTED_VOUCHER")) {
                        Voucher selectedVoucher = (Voucher) data.getSerializableExtra("SELECTED_VOUCHER");
                        applyVoucher(selectedVoucher);
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getUid();

        setupViews();
        getDataFromIntent();
        displayOrderSummary();

        // === CẬP NHẬT: Sự kiện cho nút chọn voucher ===
        layoutSelectVoucher.setOnClickListener(v -> {
            // Mở màn hình danh sách voucher và chờ kết quả trả về
            Intent intent = new Intent(CheckoutActivity.this, VoucherListActivity.class);
            voucherLauncher.launch(intent);
        });

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_checkout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        edtName = findViewById(R.id.edt_customer_name);
        edtPhone = findViewById(R.id.edt_phone);
        edtAddress = findViewById(R.id.edt_address);
        txtSubtotal = findViewById(R.id.txt_subtotal);
        txtShippingFee = findViewById(R.id.txt_shipping_fee);
        txtTotalAmount = findViewById(R.id.txt_total_amount);
        btnConfirmOrder = findViewById(R.id.btn_confirm_order);

        // Ánh xạ các view mới cho chức năng voucher
        layoutSelectVoucher = findViewById(R.id.layout_select_voucher);
        txtSelectVoucherLabel = findViewById(R.id.txt_select_voucher_label);
        layoutDiscount = findViewById(R.id.layout_discount);
        txtDiscount = findViewById(R.id.txt_discount);
    }

    private void getDataFromIntent() {
        selectedItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("SELECTED_ITEMS");
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để thanh toán", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Tính tổng tiền hàng ngay khi có dữ liệu
            for (CartItem item : selectedItems) {
                subtotal += item.getPrice() * item.getQuantity();
            }
        }
    }

    private void displayOrderSummary() {
        double discountAmount = 0;
        if (appliedVoucher != null) {
            discountAmount = appliedVoucher.getDiscountValue();
        }

        double finalTotal = subtotal + shippingFee - discountAmount;
        if (finalTotal < 0) {
            finalTotal = 0; // Đảm bảo tổng tiền không bị âm
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        txtSubtotal.setText(currencyFormatter.format(subtotal));
        txtShippingFee.setText(currencyFormatter.format(shippingFee));
        txtTotalAmount.setText(currencyFormatter.format(finalTotal));

        // Hiển thị hoặc ẩn dòng giảm giá
        if (discountAmount > 0) {
            txtDiscount.setText("- " + currencyFormatter.format(discountAmount));
            layoutDiscount.setVisibility(View.VISIBLE);
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
    }

    // Hàm mới để xử lý voucher được chọn
    private void applyVoucher(Voucher voucher) {
        if (voucher == null) return;

        // Kiểm tra điều kiện đơn hàng tối thiểu
        if (subtotal >= voucher.getMinOrderValue()) {
            appliedVoucher = voucher;
            Toast.makeText(this, "Áp dụng mã " + voucher.getCode() + " thành công!", Toast.LENGTH_SHORT).show();

            // Cập nhật lại giao diện nút "Chọn voucher"
            txtSelectVoucherLabel.setText("Đã áp dụng: " + voucher.getCode());
            txtSelectVoucherLabel.setTextColor(androidx.core.content.ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_error));
// ===========================================
            // Vô hiệu hóa nút chọn voucher sau khi đã áp dụng thành công
            layoutSelectVoucher.setEnabled(false);

            displayOrderSummary(); // Tính toán và hiển thị lại tổng tiền
        } else {
            // Thông báo lỗi nếu không đủ điều kiện
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            Toast.makeText(this, "Voucher này yêu cầu đơn hàng tối thiểu " + currencyFormatter.format(voucher.getMinOrderValue()), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmOrder() {
        String name = Objects.requireNonNull(edtName.getText()).toString().trim();
        String phone = Objects.requireNonNull(edtPhone.getText()).toString().trim();
        String address = Objects.requireNonNull(edtAddress.getText()).toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uid == null) {
            Toast.makeText(this, "Lỗi xác thực người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        double discountValue = (appliedVoucher != null) ? appliedVoucher.getDiscountValue() : 0;
        double finalPrice = subtotal + shippingFee - discountValue;
        if (finalPrice < 0) finalPrice = 0;

        Map<String, Object> order = new HashMap<>();
        order.put("userId", uid);
        order.put("totalPrice", finalPrice);
        order.put("status", "Pending");
        order.put("createdAt", FieldValue.serverTimestamp());
        order.put("customerName", name);
        order.put("customerPhone", phone);
        order.put("shippingAddress", address);
        order.put("paymentMethod", "COD");
        order.put("items", selectedItems);

        // Thêm thông tin voucher vào đơn hàng nếu có
        if (appliedVoucher != null) {
            order.put("appliedVoucher", appliedVoucher.getCode());
            order.put("discountAmount", appliedVoucher.getDiscountValue());
        }

        db.collection("orders").add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();

                    // === CẬP NHẬT: Ghi lại voucher đã sử dụng vào Firebase ===
                    if (appliedVoucher != null) {
                        Map<String, Object> usedVoucherData = new HashMap<>();
                        usedVoucherData.put("usedAt", FieldValue.serverTimestamp());
                        db.collection("users").document(uid)
                                .collection("used_vouchers").document(appliedVoucher.getCode())
                                .set(usedVoucherData);
                    }

                    // Xóa sản phẩm khỏi giỏ hàng
                    WriteBatch batch = db.batch();
                    for (CartItem item : selectedItems) {
                        batch.delete(db.collection("carts").document(uid).collection("items").document(item.getFoodId()));
                    }
                    batch.commit();

                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển màn hình và đóng các màn hình cũ
                    Intent intent = new Intent(CheckoutActivity.this, OrderTrackingActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);
                    if (CartActivity.getInstance() != null) {
                        CartActivity.getInstance().finish();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Đặt hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnConfirmOrder.setEnabled(false);
            btnConfirmOrder.setText("Đang xử lý...");
        } else {
            btnConfirmOrder.setEnabled(true);
            btnConfirmOrder.setText("Đặt Hàng");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
