package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.example.finalprojectmu.fishiohouse.models.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddFoodActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // =========================================================================
    // SỬA LỖI Ở ĐÂY: Đổi tên biến cho khớp với tên đã sử dụng bên dưới
    // =========================================================================
    private ImageView imageViewPreview;
    private EditText editTextFoodName, editTextFoodPrice, editTextFoodDesc, editTextFoodCategory;
    private Button buttonSelectImage, buttonAddFood;
    // =========================================================================

    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bây giờ các dòng findViewById này sẽ khớp với tên biến đã khai báo
        imageViewPreview = findViewById(R.id.imageViewPreview);
        editTextFoodName = findViewById(R.id.editTextFoodName);
        editTextFoodPrice = findViewById(R.id.editTextFoodPrice);
        editTextFoodDesc = findViewById(R.id.editTextFoodDesc);
        editTextFoodCategory = findViewById(R.id.editTextFoodCategory);

        buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonAddFood = findViewById(R.id.buttonAddFood);

        buttonSelectImage.setOnClickListener(v -> openFileChooser());
        buttonAddFood.setOnClickListener(v -> uploadImageAndSaveFood());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewPreview.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSaveFood() {
        // Các dòng getText() này cũng sẽ hoạt động vì tên biến đã đúng
        String name = editTextFoodName.getText().toString().trim();
        String priceStr = editTextFoodPrice.getText().toString().trim();
        String desc = editTextFoodDesc.getText().toString().trim();
        String category = editTextFoodCategory.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin và chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference("food_images/" + fileName);

        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        buttonAddFood.setEnabled(false);

        ref.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        saveFoodToFirestore(name, price, desc, downloadUri.toString(), category);
                    } else {
                        Toast.makeText(this, "Lỗi tải ảnh: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        buttonAddFood.setEnabled(true);
                    }
                });
    }

    private void saveFoodToFirestore(String name, double price, String desc, String imageUrl, String category) {
        Food food = new Food(name, price, desc, imageUrl, category);

        db.collection("foods")
                .add(food)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm món ăn thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    buttonAddFood.setEnabled(true);
                });
    }
}
