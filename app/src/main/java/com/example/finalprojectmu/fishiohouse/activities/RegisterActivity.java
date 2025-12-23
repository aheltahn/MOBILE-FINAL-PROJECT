package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextName, editTextPhone, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewGoToLogin;
    private ImageView imageViewBackground;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imageViewBackground = findViewById(R.id.imageViewBackground);
        
        // Ánh xạ các trường
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewGoToLogin = findViewById(R.id.textViewGoToLogin);

        // Tải ảnh nền
        String registerImageUrl = "https://i.pinimg.com/1200x/38/de/ff/38deff92075f558d0a0f98d82814bf33.jpg";
        Picasso.get().load(registerImageUrl).into(imageViewBackground);

        buttonRegister.setOnClickListener(v -> registerUser());

        textViewGoToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra nhập đầy đủ
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra độ dài mật khẩu
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra mật khẩu nhập lại
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Tiến hành đăng ký
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        
                        // Tạo object chứa thông tin user để lưu
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("role", "customer"); // Mặc định là khách hàng
                        user.put("avatarUrl", ""); // Mặc định chưa có avatar

                        // Lưu vào Firestore
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                    
                                    // Chuyển vào màn hình chính
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    finishAffinity(); // Đóng tất cả activity cũ để tránh quay lại màn hình đăng ký
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Lỗi khi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
