package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.adapters.CartAdapter;
import com.example.finalprojectmu.fishiohouse.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    // ================== THAY ĐỔI 1: THÊM BIẾN INSTANCE ==================
    private static CartActivity instance;
    // ===================================================================

    private RecyclerView recyclerView;
    private TextView txtTotal;
    private Button btnPlaceOrder;

    private FirebaseFirestore db;
    private String uid;

    private ArrayList<CartItem> cartItems;
    private CartAdapter cartAdapter;

    private final Map<String, Boolean> selectionState = new HashMap<>();

    // ================== THAY ĐỔI 2: THÊM HÀM LẤY INSTANCE ==================
    public static CartActivity getInstance() {
        return instance;
    }
    // =====================================================================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ================== THAY ĐỔI 3: GÁN INSTANCE ==================
        instance = this;
        // ================================================================
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        recyclerView = findViewById(R.id.recyclerViewCart);
        txtTotal = findViewById(R.id.textViewTotal);
        btnPlaceOrder = findViewById(R.id.buttonPlaceOrder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartItems, this);
        recyclerView.setAdapter(cartAdapter);

        loadCartItems();

        btnPlaceOrder.setText("Thanh Toán");
        btnPlaceOrder.setOnClickListener(v -> goToCheckout());
    }

    // --- CÁC HÀM CÒN LẠI GIỮ NGUYÊN KHÔNG ĐỔI ---

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cart_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đã trống!", Toast.LENGTH_SHORT).show();
            } else {
                showDeleteAllConfirmationDialog();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCartItems() {
        if (uid == null) return;

        db.collection("carts").document(uid).collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    for (CartItem item : cartItems) {
                        selectionState.put(item.getFoodId(), item.isSelected());
                    }

                    cartItems.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value) {
                            CartItem item = doc.toObject(CartItem.class);
                            if (item != null) {
                                item.setFoodId(doc.getId());
                                if (selectionState.containsKey(item.getFoodId())) {
                                    item.setSelected(selectionState.get(item.getFoodId()));
                                }
                                cartItems.add(item);
                            }
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();
                });
    }

    private void calculateTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtTotal.setText("Tạm tính: " + currencyFormatter.format(total));
    }

    private void goToCheckout() {
        ArrayList<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
        intent.putExtra("SELECTED_ITEMS", selectedItems);
        startActivity(intent);
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả")
                .setMessage("Bạn có chắc muốn xóa toàn bộ sản phẩm khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAllItems())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAllItems() {
        if (uid == null) return;

        Toast.makeText(this, "Đang xóa...", Toast.LENGTH_SHORT).show();

        db.collection("carts").document(uid).collection("items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            WriteBatch batch = db.batch();
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                batch.delete(doc.getReference());
                            }
                            batch.commit().addOnSuccessListener(aVoid -> {
                                Toast.makeText(CartActivity.this, "Đã xóa tất cả sản phẩm", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(CartActivity.this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        Toast.makeText(CartActivity.this, "Không thể lấy danh sách sản phẩm để xóa", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onQuantityChanged(CartItem item, long newQuantity) {
        db.collection("carts").document(uid).collection("items")
                .document(item.getFoodId()).update("quantity", newQuantity);
    }

    @Override
    public void onItemDeleted(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa '" + item.getName() + "' khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("carts").document(uid).collection("items")
                            .document(item.getFoodId()).delete();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onItemSelectionChanged() {
        calculateTotal();
    }
}
