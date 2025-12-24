package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String DEFAULT_AVATAR_URL = "https://i.pinimg.com/736x/8f/1c/a2/8f1ca2029e2efceebd22fa05cca423d7.jpg";

    private ImageView btnBack, imgAvatar;
    private TextView tvName, tvEmail, tvPhone; 
    
    private TextView btnEditProfile;
    private TextView btnChangePassword;
    private TextView btnOrderHistory;
    private Button btnLogout;

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

        initViews();
        loadUserData();
        setupEvents();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone); 
        
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());
        imgAvatar.setOnClickListener(v -> openImagePicker());

        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnEditProfile.setOnClickListener(v -> showEditNameDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi tên hiển thị");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(tvName.getText());
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                updateUserName(newName);
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUserName(String newName) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .update("name", newName) 
                    .addOnSuccessListener(aVoid -> {
                        tvName.setText(newName);
                        Toast.makeText(this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật tên", Toast.LENGTH_SHORT).show());
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        final EditText edtOldPassword = view.findViewById(R.id.edt_old_password);
        final EditText edtNewPassword = view.findViewById(R.id.edt_new_password);
        final EditText edtConfirmPassword = view.findViewById(R.id.edt_confirm_password);
        builder.setView(view);

        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            // Để trống vì sẽ override bên dưới
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPass = edtOldPassword.getText().toString();
            String newPass = edtNewPassword.getText().toString();
            String confirmPass = edtConfirmPassword.getText().toString();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(ProfileActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(ProfileActivity.this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPass.length() < 6) {
                Toast.makeText(ProfileActivity.this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            changePassword(oldPass, newPass, dialog);
        });
    }

    private void changePassword(String oldPass, String newPass, AlertDialog dialog) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.updatePassword(newPass).addOnCompleteListener(taskUpdate -> {
                        if (taskUpdate.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Lỗi đổi mật khẩu: " + taskUpdate.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ProfileActivity.this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvEmail.setText(currentUser.getEmail());

        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            String phone = document.getString("phone");
                            String avatarUrl = document.getString("avatarUrl");

                            if (name != null && !name.isEmpty()) {
                                tvName.setText(name);
                            } else {
                                tvName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Chưa có tên");
                            }

                            if (phone != null && !phone.isEmpty()) {
                                tvPhone.setText(phone);
                            } else {
                                tvPhone.setText("Chưa có số điện thoại");
                            }

                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Picasso.get().load(avatarUrl).placeholder(R.mipmap.ic_launcher_round).into(imgAvatar);
                            } else {
                                if(currentUser.getPhotoUrl() != null){
                                    Picasso.get().load(currentUser.getPhotoUrl()).placeholder(R.mipmap.ic_launcher_round).into(imgAvatar);
                                } else {
                                    Picasso.get().load(DEFAULT_AVATAR_URL).placeholder(R.mipmap.ic_launcher_round).into(imgAvatar);
                                }
                            }
                        } else {
                            tvName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Chưa có tên");
                            tvPhone.setText("Chưa có số điện thoại");
                            if(currentUser.getPhotoUrl() != null){
                                Picasso.get().load(currentUser.getPhotoUrl()).placeholder(R.mipmap.ic_launcher_round).into(imgAvatar);
                            } else {
                                Picasso.get().load(DEFAULT_AVATAR_URL).placeholder(R.mipmap.ic_launcher_round).into(imgAvatar);
                            }
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
                    }
                });
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
            imgAvatar.setImageURI(imageUri);
            uploadAvatar();
        }
    }

    private void uploadAvatar() {
        if (imageUri != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) return;
            
            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(ProfileActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateUserAvatarUrl(String avatarUrl) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .update("avatarUrl", avatarUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(ProfileActivity.this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Lỗi cập nhật Firestore", Toast.LENGTH_SHORT).show());
    }
}
