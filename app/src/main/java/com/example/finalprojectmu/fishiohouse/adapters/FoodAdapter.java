package com.example.finalprojectmu.fishiohouse.adapters;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.fishiohouse.models.Food;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public FoodAdapter(Context context, ArrayList<Food> foodList) {
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
    }

    private void addToCart(Food food) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        public FoodViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
        }
    }
}
