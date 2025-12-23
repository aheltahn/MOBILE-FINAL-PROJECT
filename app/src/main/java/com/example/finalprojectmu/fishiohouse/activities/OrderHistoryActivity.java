package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent; // THÊM VÀO
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.adapters.OrderAdapter;
import com.example.finalprojectmu.fishiohouse.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

// ================================================================
// ==== BƯỚC 1: IMPLEMENT INTERFACE CỦA ADAPTER ====
// ================================================================
public class OrderHistoryActivity extends BaseActivity implements OrderAdapter.OnOrderClickListener {

    private static final String TAG = "OrderHistoryActivity";

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private ArrayList<Order> orderList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();

        // ================================================================
        // ==== BƯỚC 2: TRUYỀN 'this' VÀO CONSTRUCTOR CỦA ADAPTER ====
        // ================================================================
        // Giờ đây, Adapter sẽ biết Activity nào đang lắng nghe nó
        orderAdapter = new OrderAdapter(this, orderList, this);
        recyclerViewOrders.setAdapter(orderAdapter);

        loadOrderHistory();
    }

    private void loadOrderHistory() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Lỗi: Người dùng không xác định", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        Toast.makeText(this, "Lỗi tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        orderList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                order.setOrderId(doc.getId());
                                orderList.add(order);
                            }
                        }
                        orderAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Tải thành công " + orderList.size() + " đơn hàng.");
                    }
                });
    }

    // ================================================================
    // ==== BƯỚC 3: VIẾT HÀM XỬ LÝ SỰ KIỆN CLICK TỪ ADAPTER ====
    // ================================================================
    @Override
    public void onOrderClick(String orderId) {
        // Khi người dùng nhấn vào một item trong RecyclerView,
        // Adapter sẽ gọi hàm này và truyền orderId vào đây.

        // Tạo Intent để mở màn hình theo dõi
        Intent intent = new Intent(OrderHistoryActivity.this, OrderTrackingActivity.class);

        // Gửi ID của đơn hàng vừa được nhấn sang
        intent.putExtra("ORDER_ID", orderId);

        // Bắt đầu Activity mới
        startActivity(intent);
    }
}
