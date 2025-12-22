package com.example.finalprojectmu.fishiohouse.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.fishiohouse.adapters.CartAdapter;
import com.example.finalprojectmu.fishiohouse.models.CartItem;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartActionListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void loadCartItems() {
    }

    private void calculateTotal() {
    }

    private void placeOrder() {
    }

    private void showDeleteAllConfirmationDialog() {
    }

    private void deleteAllItems() {
    }

    @Override
    public void onQuantityChanged(CartItem item, long newQuantity) {
    }

    @Override
    public void onItemDeleted(CartItem item) {
    }

    @Override
    public void onItemSelectionChanged() {
    }
}
