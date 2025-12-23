package com.example.finalprojectmu.fishiohouse.activities;

import android.app.Activity; // THÊM IMPORT NÀY
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    // ================== THÊM CÁC BIẾN QUẢN LÝ ACTIVITY ==================
    private static MainActivity instance;
    public static ArrayList<Activity> activityList = new ArrayList<>();
    // =====================================================================

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

    // ================== THÊM HÀM LẤY INSTANCE ==================
    public static MainActivity getInstance() {
        return instance;
    }
    // ==========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ================== THÊM CÁC DÒNG QUẢN LÝ ==================
        instance = this;
        activityList.add(this); // Thêm chính nó vào danh sách
        // =========================================================

        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerViewFoods);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        foodList = new ArrayList<>();
        fullFoodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);

        fabCart = findViewById(R.id.fab_cart);
        if (fabCart != null) {
            fabCart.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }

        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        loadCategoriesAndSetupFilter();

        searchView = findViewById(R.id.search_view);
        setupSearch();

        loadFoodsFromFirestore();
    }

    // ================== THÊM HÀM ĐÓNG ACTIVITY CON ==================
    public void finishChildActivities() {
        for (Activity activity : activityList) {
            if (activity != null && !(activity instanceof MainActivity)) {
                activity.finish();
            }
        }
        activityList.clear();
        activityList.add(this); // Giữ lại MainActivity
    }
    // =============================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityList.remove(this); // Xóa activity khỏi danh sách khi bị hủy
    }

    // --- CÁC HÀM CÒN LẠI CỦA BẠN GIỮ NGUYÊN KHÔNG ĐỔI ---

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
                currentSearchText = newText;
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
                if (selectedChip != null) {
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
            String foodCategory = food.getCategory() != null ? food.getCategory() : "";

            boolean matchCategory = currentCategory.equals("all") || foodCategory.equalsIgnoreCase(currentCategory);
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
