package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(this);

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

        // Bước 1: Load dữ liệu từ SQLite trước (để dùng offline)
        loadFoodsFromLocal();

        // Bước 2: Load dữ liệu từ Firestore (để lấy mới nhất và sync về local)
        loadFoodsFromFirestore();
    }

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
                        Log.e(TAG, "Listen failed.", error);
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
                                    if (food.getType() == null) {
                                        food.setType("other");
                                    }
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
                                Log.e(TAG, "Lỗi đọc dữ liệu món ăn: " + doc.getId(), e);
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