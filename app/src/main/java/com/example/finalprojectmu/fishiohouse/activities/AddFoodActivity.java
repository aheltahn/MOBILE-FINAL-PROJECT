package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

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
    }

    private void openFileChooser() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    }

    private void uploadImageAndSaveFood() {
    }

    private void saveFoodToFirestore(String name, double price, String desc, String imageUrl) {
    }
}