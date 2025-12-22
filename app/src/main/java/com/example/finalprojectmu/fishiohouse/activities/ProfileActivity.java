package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageViewAvatar;
    private TextView textViewUserEmail;
    private Button buttonChangeAvatar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewUserEmail = findViewById(R.id.textViewUserEmail);
        buttonChangeAvatar = findViewById(R.id.buttonChangeAvatar);

        loadUserData();

        buttonChangeAvatar.setOnClickListener(v -> openImagePicker());
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            textViewUserEmail.setText(currentUser.getEmail());

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Picasso.get().load(avatarUrl).into(imageViewAvatar);
                            }
                        }
                    });
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewAvatar.setImageURI(imageUri); // Hiển thị ảnh đã chọn ngay lập tức
            uploadAvatar();
        }
    }

    private void uploadAvatar() {
        if (imageUri != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;

            StorageReference storageRef = storage.getReference("avatars/" + user.getUid() + ".jpg");

            storageRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            updateUserAvatarUrl(downloadUri.toString());
                        } else {
                            Toast.makeText(ProfileActivity.this, "Lỗi tải lên ảnh: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUserAvatarUrl(String avatarUrl) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Lỗi cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show());
    }
}
