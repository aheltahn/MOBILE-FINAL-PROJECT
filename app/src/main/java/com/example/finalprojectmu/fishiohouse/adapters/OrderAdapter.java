package com.example.finalprojectmu.fishiohouse.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.fishiohouse.models.Order;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public OrderAdapter(Context context, ArrayList<Order> orderList) {
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        public OrderViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
        }
    }
}
