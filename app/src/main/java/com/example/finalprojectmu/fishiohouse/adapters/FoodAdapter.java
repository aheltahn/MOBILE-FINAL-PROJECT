package com.example.finalprojectmu.fishiohouse.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Sửa ở đây, không dùng Button nữa
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final Context context;
    private final ArrayList<Food> foodList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

    public FoodAdapter(Context context, ArrayList<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        if (food == null) return;

        holder.name.setText(food.getName());
        holder.description.setText(food.getDescription()); // Thêm dòng này

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.price.setText(currencyFormatter.format(food.getPrice()));

        if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
            Picasso.get().load(food.getImageUrl()).into(holder.image);
        } else {
            holder.image.setImageResource(R.mipmap.ic_launcher); // Ảnh mặc định
        }

        holder.addToCartButton.setOnClickListener(v -> {
            if (uid == null) {
                Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }
            addToCart(food);
        });
    }

    private void addToCart(Food food) {
        if (food.getId() == null) {
            Toast.makeText(context, "Lỗi: Món ăn không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        DocumentReference cartItemRef = db.collection("carts").document(uid).collection("items").document(food.getId());

        db.runTransaction(transaction -> {
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
            Toast.makeText(context, "Đã thêm '" + food.getName() + "' vào giỏ", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, price, description; // Thêm description
        ImageView addToCartButton; // SỬA Ở ĐÂY: từ Button sang ImageView

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageViewFood);
            name = itemView.findViewById(R.id.textViewFoodName);
            price = itemView.findViewById(R.id.textViewFoodPrice);
            description = itemView.findViewById(R.id.textViewFoodDesc); // Thêm dòng này
            addToCartButton = itemView.findViewById(R.id.buttonAddToCart); // Sửa ở đây
        }
    }
}
