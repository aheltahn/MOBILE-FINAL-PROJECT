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

    private ImageView imageViewPreview;
    private EditText editName, editPrice, editDesc;
    private Button btnSelectImage, btnAddFood;

    private Uri imageUri;
    private FirebaseStorage storage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        imageViewPreview = findViewById(R.id.imageViewPreview);
        editName = findViewById(R.id.editTextFoodName);
        editPrice = findViewById(R.id.editTextFoodPrice);
        editDesc = findViewById(R.id.editTextFoodDesc);
        btnSelectImage = findViewById(R.id.buttonSelectImage);
        btnAddFood = findViewById(R.id.buttonAddFood);

        btnSelectImage.setOnClickListener(v -> openFileChooser());

        btnAddFood.setOnClickListener(v -> uploadImageAndSaveFood());
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
        String name = editName.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();
        String desc = editDesc.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Vui lòng nhập đủ tên, giá và chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        // Tạo tên file ngẫu nhiên để không bị trùng
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference ref = storage.getReference("food_images/" + fileName);

        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        btnAddFood.setEnabled(false); // Khóa nút để tránh bấm nhiều lần

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
                        saveFoodToFirestore(name, price, desc, downloadUri.toString());
                    } else {
                        Toast.makeText(this, "Lỗi tải ảnh: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        btnAddFood.setEnabled(true);
                    }
                });
    }

    private void saveFoodToFirestore(String name, double price, String desc, String imageUrl) {
        Food food = new Food(name, price, desc, imageUrl);

        db.collection("foods")
                .add(food)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Thêm món ăn thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnAddFood.setEnabled(true);
                });
    }
}