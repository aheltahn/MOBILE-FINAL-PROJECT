package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public abstract class BaseActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupHeader();
    }

    private void setupHeader() {
        mAuth = FirebaseAuth.getInstance();
        View headerView = findViewById(R.id.main_header);
        if (headerView == null) return;

        TextView menuButton = headerView.findViewById(R.id.header_menu_button);
        ImageView avatar = headerView.findViewById(R.id.header_avatar);

        // SỬA Ở ĐÂY: Thêm kiểm tra null trước khi gán sự kiện
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                if (!(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                }
            });
        }

        if (avatar != null) {
            avatar.setOnClickListener(this::showAvatarMenu);
            loadUserAvatar(avatar);
        }
    }

    private void showAvatarMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.avatar_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.action_order_history) {
                startActivity(new Intent(this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.action_logout) {
                mAuth.signOut();
                Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void loadUserAvatar(ImageView avatar) {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            String avatarUrl = doc.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Picasso.get().load(avatarUrl).into(avatar);
                            }
                        }
                    });
        }
    }
}
