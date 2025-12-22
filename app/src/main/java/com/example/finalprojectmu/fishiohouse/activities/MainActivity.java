package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.adapters.FoodAdapter;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

// SỬA Ở ĐÂY: Thừa kế lại từ BaseActivity
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<Food> foodList;
    private ArrayList<Food> fullFoodList;
    private FoodAdapter foodAdapter;
    private FloatingActionButton fabCart;
    private ChipGroup chipGroupCategories;
    private SearchView searchView;

    private String currentCategory = "all";
    private String currentSearchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Header đã được BaseActivity xử lý, không cần code ở đây

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewFoods);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        foodList = new ArrayList<>();
        fullFoodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);

        fabCart = findViewById(R.id.fab_cart);
        if (fabCart != null) {
            fabCart.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
        }

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        setupCategoryFilter();

        searchView = findViewById(R.id.search_view);
        setupSearch();

        loadFoodsFromFirestore();
    }

    private void loadFoodsFromFirestore() {
        db.collection("foods")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        fullFoodList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Food food = doc.toObject(Food.class);
                                if (food != null) {
                                    food.setId(doc.getId());
                                    if (food.getType() == null) {
                                        food.setType("other");
                                    }
                                    fullFoodList.add(food);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi đọc dữ liệu món ăn: " + doc.getId(), e);
                            }
                        }
                        applyFilters();
                    }
                });
    }

    private void setupSearch() {
        if (searchView == null) return;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchText = newText;
                applyFilters();
                return true;
            }
        });
    }

    private void setupCategoryFilter() {
        if (chipGroupCategories == null) return;

        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentCategory = "all";
            } else if (checkedId == R.id.chipSushi) {
                currentCategory = "sushi";
            } else if (checkedId == R.id.chipSoup) {
                currentCategory = "soup";
            } else if (checkedId == R.id.chipDrink) {
                currentCategory = "drink";
            } else {
                currentCategory = "all";
            }
            applyFilters();
        });
    }

    private void applyFilters() {
        if (fullFoodList == null) return;

        foodList.clear();
        for (Food food : fullFoodList) {
            boolean matchCategory = currentCategory.equals("all") ||
                    (food.getType() != null && food.getType().equalsIgnoreCase(currentCategory));
            boolean matchSearch = currentSearchText.isEmpty() ||
                    (food.getName() != null && food.getName().toLowerCase().contains(currentSearchText.toLowerCase()));

            if (matchCategory && matchSearch) {
                foodList.add(food);
            }
        }
        if (foodAdapter != null) {
            foodAdapter.notifyDataSetChanged();
        }
    }
}
