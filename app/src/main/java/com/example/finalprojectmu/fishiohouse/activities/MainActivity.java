package com.example.finalprojectmu.fishiohouse.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.adapters.FoodAdapter;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    private static MainActivity instance;
    public static ArrayList<Activity> activityList = new ArrayList<>();

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ArrayList<Food> foodList;
    private ArrayList<Food> fullFoodList;
    private FoodAdapter foodAdapter;
    private FloatingActionButton fabCart;
    private ChipGroup chipGroupCategories;
    private SearchView searchView;
    private Spinner spinnerSort;

    private String currentCategory = "all";
    private String currentSearchText = "";
    private int currentSortMode = 0;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        activityList.add(this);

        setContentView(R.layout.activity_main);
        
        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewFoods);
        fabCart = findViewById(R.id.fab_cart);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        searchView = findViewById(R.id.search_view);
        spinnerSort = findViewById(R.id.spinner_sort);

        foodList = new ArrayList<>();
        fullFoodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);

        updateGridLayout();

        if (fabCart != null) {
            fabCart.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }

        loadCategoriesAndSetupFilter();
        setupSearch();
        setupSortSpinner(); 
        loadFoodsFromFirestore();
    }

    private void updateGridLayout() {
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 4 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGridLayout();
    }
    
    public void finishChildActivities() {
        for (Activity activity : activityList) {
            if (activity != null && !(activity instanceof MainActivity)) {
                activity.finish();
            }
        }
        activityList.clear();
        activityList.add(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityList.remove(this);
    }

    private void setupSortSpinner() {
        if (spinnerSort == null) return;

        String[] sortOptions = {"Mặc định", "Giá: Thấp đến Cao", "Giá: Cao đến Thấp"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_spinner_selected, sortOptions);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortMode = position;
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadFoodsFromFirestore() {
        db.collection("foods")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Lỗi khi tải món ăn.", error);
                        return;
                    }

                    if (value != null) {
                        fullFoodList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Food food = doc.toObject(Food.class);
                                if (food != null) {
                                    food.setId(doc.getId());
                                    if (food.getType() == null) food.setType("other");
                                    
                                    fullFoodList.add(food);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi món ăn: " + doc.getId(), e);
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
                currentSearchText = newText.toLowerCase().trim();
                applyFilters();
                return true;
            }
        });
    }

    private void loadCategoriesAndSetupFilter() {
        if (chipGroupCategories == null) return;
        chipGroupCategories.removeAllViews();

        addCategoryChip("Tất cả", "all", true);

        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String categoryName = doc.getString("name");
                        String categoryId = doc.getString("id");
                        if (categoryName != null && categoryId != null) {
                            addCategoryChip(categoryName, categoryId, false);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi tải danh mục", e));

        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == -1) {
                currentCategory = "all";
            } else {
                Chip selectedChip = findViewById(checkedId);
                if (selectedChip != null && selectedChip.getTag() != null) {
                    currentCategory = selectedChip.getTag().toString();
                }
            }
            applyFilters();
        });
    }

    private void addCategoryChip(String name, String id, boolean isChecked) {
        Chip chip = new Chip(this);
        chip.setText(name);
        chip.setTag(id);
        chip.setCheckable(true);
        chip.setChecked(isChecked);
        chip.setId(android.view.View.generateViewId());
        chipGroupCategories.addView(chip);
    }

    private void applyFilters() {
        if (fullFoodList == null) return;

        foodList.clear();
        
        for (Food food : fullFoodList) {
            String foodType = food.getType() != null ? food.getType() : "";
            
            boolean matchCategory = currentCategory.equals("all") || 
                                    foodType.equalsIgnoreCase(currentCategory);
                                    
            boolean matchSearch = currentSearchText.isEmpty() ||
                    (food.getName() != null && food.getName().toLowerCase().contains(currentSearchText));

            if (matchCategory && matchSearch) {
                foodList.add(food);
            }
        }

        if (currentSortMode == 1) { // Giá tăng dần
            Collections.sort(foodList, (f1, f2) -> Double.compare(f1.getPrice(), f2.getPrice()));
        } else if (currentSortMode == 2) { // Giá giảm dần
            Collections.sort(foodList, (f1, f2) -> Double.compare(f2.getPrice(), f1.getPrice()));
        }

        if (foodAdapter != null) {
            foodAdapter.notifyDataSetChanged();
        }
    }
}
