package com.example.finalprojectmu.fishiohouse.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoodDetailActivity extends AppCompatActivity {

    private ImageView imgFoodDetail;
    private TextView txtFoodName, txtFoodDescription, txtQuantity, txtTotalPrice;
    private ImageButton btnDecrease, btnIncrease;
    private Button btnAddToCart;

    private Food food;
    private int quantity = 1;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        initViews();
        getFoodData();
        setupEventListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_food_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        imgFoodDetail = findViewById(R.id.img_food_detail);
        txtFoodName = findViewById(R.id.txt_food_name_detail);
        txtFoodDescription = findViewById(R.id.txt_food_description_detail);
        txtQuantity = findViewById(R.id.txt_quantity);
        txtTotalPrice = findViewById(R.id.txt_total_price_detail);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        btnAddToCart = findViewById(R.id.btn_add_to_cart_detail);
    }

    private void getFoodData() {
        food = (Food) getIntent().getSerializableExtra("FOOD_DETAIL");
        if (food != null) {
            displayFoodDetails();
        } else {
            Toast.makeText(this, "Không thể tải thông tin món ăn", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayFoodDetails() {
        txtFoodName.setText(food.getName());
        txtFoodDescription.setText(food.getDescription());

        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Picasso.get().load(food.getImageUrl()).into(imgFoodDetail);
        }

        updatePrice();
    }

    private void setupEventListeners() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            txtQuantity.setText(String.valueOf(quantity));
            updatePrice();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                txtQuantity.setText(String.valueOf(quantity));
                updatePrice();
            }
        });

        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void updatePrice() {
        double totalPrice = food.getPrice() * quantity;
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        txtTotalPrice.setText(currencyFormatter.format(totalPrice));
    }

    private void addToCart() {
        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference cartItemRef = db.collection("carts").document(uid).collection("items").document(food.getId());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(cartItemRef);
            if (snapshot.exists()) {
                Long currentQuantity = snapshot.getLong("quantity");
                if (currentQuantity == null) currentQuantity = 0L;
                transaction.update(cartItemRef, "quantity", currentQuantity + quantity);
            } else {
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("name", food.getName());
                cartItem.put("price", food.getPrice());
                cartItem.put("quantity", (long) quantity);
                cartItem.put("imageUrl", food.getImageUrl());
                cartItem.put("foodId", food.getId());
                transaction.set(cartItemRef, cartItem);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            finish(); // Đóng trang chi tiết sau khi thêm thành công
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
