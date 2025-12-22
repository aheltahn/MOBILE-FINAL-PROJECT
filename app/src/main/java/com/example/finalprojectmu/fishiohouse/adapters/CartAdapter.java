package com.example.finalprojectmu.fishiohouse.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.fishiohouse.models.CartItem;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    public interface OnCartActionListener {
        void onQuantityChanged(CartItem item, long newQuantity);
        void onItemDeleted(CartItem item);
        void onItemSelectionChanged();
    }

    public CartAdapter(Context context, ArrayList<CartItem> cartItems, OnCartActionListener listener) {
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
        }
    }
}
