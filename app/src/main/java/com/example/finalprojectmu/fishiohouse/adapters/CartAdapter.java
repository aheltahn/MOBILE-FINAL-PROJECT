package com.example.finalprojectmu.fishiohouse.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.CartItem;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<CartItem> cartItems;
    private final OnCartActionListener listener;

    // Interface để giao tiếp với Activity
    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, long newQuantity);
        void onItemDeleted(CartItem item);
        void onItemSelectionChanged(); // Gọi khi checkbox thay đổi
    }

    public CartAdapter(Context context, ArrayList<CartItem> cartItems, OnCartActionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.name.setText(cartItem.getName());

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.price.setText(currencyFormatter.format(cartItem.getPrice()));

        holder.quantity.setText(String.valueOf(cartItem.getQuantity()));

        if (cartItem.getImageUrl() != null && !cartItem.getImageUrl().isEmpty()) {
            Picasso.get().load(cartItem.getImageUrl()).into(holder.image);
        }

        // Checkbox state
        holder.checkBox.setOnCheckedChangeListener(null); // Tránh loop vô hạn
        holder.checkBox.setChecked(cartItem.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cartItem.setSelected(isChecked);
            listener.onItemSelectionChanged();
        });

        // Nút giảm
        holder.btnDecrease.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                listener.onQuantityChanged(cartItem, cartItem.getQuantity() - 1);
            } else {
                // Nếu giảm về 0 thì hỏi xóa
                listener.onItemDeleted(cartItem);
            }
        });

        // Nút tăng
        holder.btnIncrease.setOnClickListener(v -> {
            listener.onQuantityChanged(cartItem, cartItem.getQuantity() + 1);
        });

        // Nút xóa
        holder.btnDelete.setOnClickListener(v -> listener.onItemDeleted(cartItem));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageView image, btnDelete, btnDecrease, btnIncrease;
        TextView name, price, quantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBoxItem);
            image = itemView.findViewById(R.id.imageViewCartItem);
            name = itemView.findViewById(R.id.textViewCartItemName);
            price = itemView.findViewById(R.id.textViewCartItemPrice);
            btnDelete = itemView.findViewById(R.id.buttonDeleteItem);
            btnDecrease = itemView.findViewById(R.id.buttonDecreaseQuantity);
            quantity = itemView.findViewById(R.id.textViewQuantity);
            btnIncrease = itemView.findViewById(R.id.buttonIncreaseQuantity);
        }
    }
}
