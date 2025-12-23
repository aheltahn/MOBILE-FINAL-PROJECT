package com.example.finalprojectmu.fishiohouse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.adapters.VoucherAdapter;
import com.example.finalprojectmu.fishiohouse.models.Voucher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class VoucherListActivity extends AppCompatActivity {

    private static final String TAG = "VoucherListActivity";

    private RecyclerView recyclerView;
    private VoucherAdapter voucherAdapter;
    private ArrayList<Voucher> availableVouchers;
    private FirebaseFirestore db;
    private String uid;
    private ProgressBar progressBar;
    private TextView txtNoVouchers;
    private Button btnConfirm;
    private Voucher selectedVoucher = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_list);

        // Khởi tạo Firebase
        db = FirebaseFirestore.getInstance();
        uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Ánh xạ Views
        setupViews();

        // Thiết lập RecyclerView
        availableVouchers = new ArrayList<>();
        voucherAdapter = new VoucherAdapter(availableVouchers, voucher -> {
            selectedVoucher = voucher; // Lưu voucher được chọn
        });
        recyclerView.setAdapter(voucherAdapter);

        // Tải danh sách vouchers
        loadVouchers();

        // Xử lý nút xác nhận
        btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void setupViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_voucher_list);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_view_vouchers);
        progressBar = findViewById(R.id.progress_bar);
        txtNoVouchers = findViewById(R.id.txt_no_vouchers);
        btnConfirm = findViewById(R.id.btn_confirm_voucher);
    }

    private void loadVouchers() {
        progressBar.setVisibility(View.VISIBLE);

        // B1: Lấy danh sách ID các voucher đã sử dụng
        db.collection("users").document(uid).collection("used_vouchers")
                .get()
                .addOnSuccessListener(usedVouchersSnapshot -> {
                    HashSet<String> usedVoucherIds = new HashSet<>();
                    for (QueryDocumentSnapshot doc : usedVouchersSnapshot) {
                        usedVoucherIds.add(doc.getId());
                    }

                    // B2: Lấy tất cả voucher công khai
                    db.collection("vouchers")
                            .get()
                            .addOnSuccessListener(allVouchersSnapshot -> {
                                availableVouchers.clear();
                                for (QueryDocumentSnapshot doc : allVouchersSnapshot) {
                                    // B3: Chỉ thêm vào danh sách nếu voucher đó CHƯA được sử dụng
                                    if (!usedVoucherIds.contains(doc.getId())) {
                                        Voucher voucher = doc.toObject(Voucher.class);
                                        availableVouchers.add(voucher);
                                    }
                                }

                                // Cập nhật giao diện
                                progressBar.setVisibility(View.GONE);
                                if (availableVouchers.isEmpty()) {
                                    txtNoVouchers.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                } else {
                                    txtNoVouchers.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    voucherAdapter.notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e(TAG, "Lỗi khi tải danh sách voucher công khai", e);
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Lỗi khi tải danh sách voucher đã dùng", e);
                });
    }

    private void confirmSelection() {
        if (selectedVoucher == null) {
            Toast.makeText(this, "Bạn chưa chọn voucher nào", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo intent để gửi dữ liệu về CheckoutActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SELECTED_VOUCHER", selectedVoucher);
        setResult(Activity.RESULT_OK, resultIntent);
        finish(); // Đóng màn hình hiện tại và quay về
    }
}
