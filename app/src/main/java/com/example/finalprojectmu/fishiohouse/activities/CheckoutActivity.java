package com.example.finalprojectmu.fishiohouse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.finalprojectmu.fishiohouse.models.Voucher;
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

    private TextInputEditText edtName, edtPhone, edtAddress;
    private TextView txtSubtotal, txtShippingFee, txtTotalAmount, txtDiscount, txtSelectVoucherLabel;
    private Button btnConfirmOrder;
    private RelativeLayout layoutDiscount, layoutSelectVoucher;

    private double subtotal = 0;
    private final double shippingFee = 15000;
    private Voucher appliedVoucher = null;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<CartItem> selectedItems;
    private String uid;

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

        layoutSelectVoucher.setOnClickListener(v -> {
            Intent intent = new Intent(CheckoutActivity.this, VoucherListActivity.class);
            voucherLauncher.launch(intent);
        });

        btnConfirmOrder.setOnClickListener(v -> confirmOrder());
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_checkout);
        // THÊM KIỂM TRA NULL ĐỂ TRÁNH CRASH
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            Log.e("CheckoutActivity", "Không tìm thấy Toolbar với ID R.id.toolbar_checkout");
        }

        edtName = findViewById(R.id.edt_customer_name);
        edtPhone = findViewById(R.id.edt_phone);
        edtAddress = findViewById(R.id.edt_address);
        txtSubtotal = findViewById(R.id.txt_subtotal);
        txtShippingFee = findViewById(R.id.txt_shipping_fee);
        txtTotalAmount = findViewById(R.id.txt_total_amount);
        btnConfirmOrder = findViewById(R.id.btn_confirm_order);

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
            finalTotal = 0;
        }

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        txtSubtotal.setText(currencyFormatter.format(subtotal));
        txtShippingFee.setText(currencyFormatter.format(shippingFee));
        txtTotalAmount.setText(currencyFormatter.format(finalTotal));

        if (discountAmount > 0) {
            txtDiscount.setText("- " + currencyFormatter.format(discountAmount));
            layoutDiscount.setVisibility(View.VISIBLE);
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
    }

    private void applyVoucher(Voucher voucher) {
        if (voucher == null) return;

        if (subtotal >= voucher.getMinOrderValue()) {
            appliedVoucher = voucher;
            Toast.makeText(this, "Áp dụng mã " + voucher.getCode() + " thành công!", Toast.LENGTH_SHORT).show();

            txtSelectVoucherLabel.setText("Đã áp dụng: " + voucher.getCode());
            txtSelectVoucherLabel.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.theme_accent_orange));
            layoutSelectVoucher.setEnabled(false);

            displayOrderSummary();
        } else {
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

        if (appliedVoucher != null) {
            order.put("appliedVoucher", appliedVoucher.getCode());
            order.put("discountAmount", appliedVoucher.getDiscountValue());
        }

        db.collection("orders").add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();

                    if (appliedVoucher != null) {
                        Map<String, Object> usedVoucherData = new HashMap<>();
                        usedVoucherData.put("usedAt", FieldValue.serverTimestamp());
                        db.collection("users").document(uid)
                                .collection("used_vouchers").document(appliedVoucher.getCode())
                                .set(usedVoucherData);
                    }

                    WriteBatch batch = db.batch();
                    for (CartItem item : selectedItems) {
                        batch.delete(db.collection("carts").document(uid).collection("items").document(item.getFoodId()));
                    }
                    batch.commit();

                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CheckoutActivity.this, OrderTrackingActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);

                    finish(); // Chỉ đóng màn hình checkout
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
