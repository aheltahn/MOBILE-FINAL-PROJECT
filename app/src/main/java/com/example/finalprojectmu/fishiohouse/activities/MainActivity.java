package com.example.finalprojectmu.fishiohouse.activities;

import android.app.Activity; // THÊM IMPORT NÀY
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.adapters.FoodAdapter;
import com.example.finalprojectmu.fishiohouse.data.DatabaseHelper;
import com.example.finalprojectmu.fishiohouse.data.ProductEntity;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    // ================== THÊM CÁC BIẾN QUẢN LÝ ACTIVITY ==================
    private static MainActivity instance;
    public static ArrayList<Activity> activityList = new ArrayList<>();
    // =====================================================================

    private FirebaseFirestore db;
    private DatabaseHelper dbHelper; // Thêm DatabaseHelper
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
        fabCart = findViewById(R.id.fab_cart);
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        searchView = findViewById(R.id.search_view);

        foodList = new ArrayList<>();
        fullFoodList = new ArrayList<>();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);

        // === RESPONSIVE: TỰ ĐỘNG THAY ĐỔI SỐ CỘT THEO HƯỚNG MÀN HÌNH ===
        updateGridLayout();

        if (fabCart != null) {
            fabCart.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }

        setupCategoryFilter();
        setupSearch();
        loadFoodsFromFirestore();
    }

    // Hàm này sẽ được gọi khi xoay màn hình (onConfigurationChanged) hoặc lúc khởi tạo
    private void updateGridLayout() {
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 4 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
    }

    // Để xử lý khi xoay màn hình mà không recreate Activity (tùy chọn, mượt hơn)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGridLayout(); // Cập nhật lại số cột khi xoay
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        loadCategoriesAndSetupFilter();

        searchView = findViewById(R.id.search_view);
        setupSearch();

        // Bước 1: Load dữ liệu từ SQLite trước (để dùng offline)
        loadFoodsFromLocal();

        // Bước 2: Load dữ liệu từ Firestore (để lấy mới nhất và sync về local)
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
    // Hàm load dữ liệu từ SQLite
    private void loadFoodsFromLocal() {
        List<ProductEntity> localProducts = dbHelper.getAllProducts();
        if (localProducts != null && !localProducts.isEmpty()) {
            fullFoodList.clear();
            for (ProductEntity entity : localProducts) {
                Food food = new Food(
                        entity.getName(),
                        entity.getPrice(),
                        entity.getDescription(),
                        entity.getImageUrl(),
                        entity.getType()
                );
                food.setId(entity.getId());
                fullFoodList.add(food);
            }
            applyFilters();
            // Toast.makeText(this, "Đã tải " + fullFoodList.size() + " món ăn từ bộ nhớ máy", Toast.LENGTH_SHORT).show();
        }
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
                        List<ProductEntity> productsToSync = new ArrayList<>();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                Food food = doc.toObject(Food.class);
                                if (food != null) {
                                    food.setId(doc.getId());
                                    fullFoodList.add(food);

                                    // Tạo đối tượng Entity để lưu vào SQLite
                                    productsToSync.add(new ProductEntity(
                                            food.getId(),
                                            food.getName(),
                                            food.getPrice(),
                                            food.getDescription(),
                                            food.getImageUrl(),
                                            food.getType()
                                    ));
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi chuyển đổi món ăn: " + doc.getId(), e);
                            }
                        }
                        
                        // Cập nhật giao diện
                        applyFilters();

                        // Bước 3: Đồng bộ dữ liệu về SQLite (Sync)
                        syncDataToLocal(productsToSync);
                    }
                });
    }

    // Hàm lưu dữ liệu vào SQLite
    private void syncDataToLocal(List<ProductEntity> products) {
        new Thread(() -> {
            // Xóa dữ liệu cũ để đảm bảo đồng bộ chính xác với server (bao gồm cả việc xóa món ăn)
            dbHelper.deleteAllProducts();
            
            // Lưu dữ liệu mới
            for (ProductEntity product : products) {
                dbHelper.insertOrUpdateProduct(product);
            }
            Log.d(TAG, "Đã đồng bộ " + products.size() + " món ăn về SQLite");
        }).start();
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
            boolean matchCategory = currentCategory.equals("all") ||
                    (food.getType() != null && food.getType().equalsIgnoreCase(currentCategory));

            String foodCategory = food.getCategory() != null ? food.getCategory() : "";

            boolean matchCategory = currentCategory.equals("all") || foodCategory.equalsIgnoreCase(currentCategory);
            boolean matchSearch = currentSearchText.isEmpty() ||
                    (food.getName() != null && food.getName().toLowerCase().contains(currentSearchText));

            if (matchCategory && matchSearch) {
                foodList.add(food);
            }
        }

        if (foodAdapter != null) {
            foodAdapter.notifyDataSetChanged();
        }
    }
}