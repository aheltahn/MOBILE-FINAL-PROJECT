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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    // URL ảnh đại diện mặc định
    private static final String DEFAULT_AVATAR_URL = "https://i.pinimg.com/736x/8f/1c/a2/8f1ca2029e2efceebd22fa05cca423d7.jpg";

    private ImageView btnBack, imgAvatar;
    private TextView tvName, tvEmail;
    
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

        // Chức năng: Chỉnh sửa tên hiển thị
        btnEditProfile.setOnClickListener(v -> showEditNameDialog());

        // Chức năng: Đổi mật khẩu trực tiếp
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    // --- HIỂN THỊ HỘP THOẠI ĐỔI TÊN ---
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
                    .update("fullName", newName)
                    .addOnSuccessListener(aVoid -> {
                        tvName.setText(newName);
                        Toast.makeText(this, "Cập nhật tên thành công", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật tên", Toast.LENGTH_SHORT).show());
        }
    }

    // --- HIỂN THỊ HỘP THOẠI ĐỔI MẬT KHẨU ---
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        // Tạo layout cho dialog bằng code (hoặc có thể dùng XML riêng)
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText edtOldPassword = new EditText(this);
        edtOldPassword.setHint("Mật khẩu cũ");
        edtOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(edtOldPassword);

        final EditText edtNewPassword = new EditText(this);
        edtNewPassword.setHint("Mật khẩu mới");
        edtNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(edtNewPassword);

        final EditText edtConfirmPassword = new EditText(this);
        edtConfirmPassword.setHint("Nhập lại mật khẩu mới");
        edtConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(edtConfirmPassword);

        builder.setView(layout);

        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            // Sẽ xử lý trong listener của nút Positive để ngăn dialog đóng nếu lỗi,
            // nhưng AlertDialog mặc định sẽ đóng. Ta sẽ override ở dưới nếu cần hoặc xử lý logic đơn giản ở đây.
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override sự kiện click nút Positive để kiểm tra hợp lệ
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
            // 1. Xác thực lại người dùng bằng mật khẩu cũ
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // 2. Nếu xác thực thành công, tiến hành đổi mật khẩu
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
        if (currentUser != null) {
            tvEmail.setText(currentUser.getEmail());
            
            String name = currentUser.getEmail();
            if (name != null && name.contains("@")) {
                name = name.split("@")[0];
            }
            tvName.setText(name);
            
            // Đặt ảnh mặc định trước khi tải
            Picasso.get()
                    .load(DEFAULT_AVATAR_URL)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .into(imgAvatar);

            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            if (fullName != null && !fullName.isEmpty()) {
                                tvName.setText(fullName);
                            }

                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            // Nếu có avatarUrl từ Firestore thì load, nếu không thì giữ nguyên ảnh mặc định
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Picasso.get()
                                        .load(avatarUrl)
                                        .placeholder(R.mipmap.ic_launcher_round)
                                        .error(R.mipmap.ic_launcher_round)
                                        .into(imgAvatar);
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