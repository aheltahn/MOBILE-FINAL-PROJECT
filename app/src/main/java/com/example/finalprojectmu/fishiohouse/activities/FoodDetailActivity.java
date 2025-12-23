package com.example.finalprojectmu.fishiohouse.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoodDetailActivity extends AppCompatActivity {

    private Food food;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Dùng chính item_food.xml làm layout chi tiết
        setContentView(R.layout.item_food);

        ImageView imageViewFood = findViewById(R.id.imageViewFood);
        TextView textViewName = findViewById(R.id.textViewFoodName);
        TextView textViewDesc = findViewById(R.id.textViewFoodDesc);
        TextView textViewPrice = findViewById(R.id.textViewFoodPrice);
        ImageView buttonAddToCart = findViewById(R.id.buttonAddToCart);

        food = (Food) getIntent().getSerializableExtra("FOOD_DETAIL");
        uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (food != null) {
            textViewName.setText(food.getName());

            if (textViewDesc != null) {
                textViewDesc.setText(food.getDescription());
            }

            // Định dạng giá tiền Việt Nam
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textViewPrice.setText(currencyFormat.format(food.getPrice()));

            // Tải ảnh lớn
            if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(food.getImageUrl())
                        .fit()
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher_round)
                        .into(imageViewFood);
            }

            // === KHI BẤM NÚT + → THÊM 1 MÓN VÀO GIỎ HÀNG ===
            buttonAddToCart.setOnClickListener(v -> addOneToCart());
        }
    }

    private void addOneToCart() {
        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference cartItemRef = db.collection("carts")
                .document(uid)
                .collection("items")
                .document(food.getId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(cartItemRef);

            if (snapshot.exists()) {
                Long currentQuantity = snapshot.getLong("quantity");
                if (currentQuantity == null) currentQuantity = 0L;
                transaction.update(cartItemRef, "quantity", currentQuantity + 1);
            } else {
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("name", food.getName());
                cartItem.put("price", food.getPrice());
                cartItem.put("quantity", 1L);
                cartItem.put("imageUrl", food.getImageUrl());
                cartItem.put("foodId", food.getId());
                transaction.set(cartItemRef, cartItem);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã thêm '" + food.getName() + "' vào giỏ hàng", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}