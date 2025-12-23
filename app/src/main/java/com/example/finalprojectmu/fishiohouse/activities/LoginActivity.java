package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalprojectmu.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleActivity";

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonGoogleLogin;
    private TextView textViewGoToRegister;
    private ImageView imageViewBackground;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Kiểm tra nếu đã đăng nhập thì chuyển luôn vào Home
        if (mAuth.getCurrentUser() != null) {
            updateUI(mAuth.getCurrentUser());
            return;
        }

        setContentView(R.layout.activity_login);

        // Cấu hình Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        imageViewBackground = findViewById(R.id.imageViewBackground);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoogleLogin = findViewById(R.id.buttonGoogleLogin);
        textViewGoToRegister = findViewById(R.id.textViewGoToRegister);

        // Tải ảnh nền
        String loginImageUrl = "https://i.pinimg.com/736x/9b/7d/cc/9b7dcc40ab054e585ad7f57060c1d9f2.jpg";
        Picasso.get().load(loginImageUrl).into(imageViewBackground);

        buttonLogin.setOnClickListener(v -> loginUser());

        buttonGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        textViewGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kết quả trả về từ Intent đăng nhập Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Đăng nhập Google thành công, xác thực với Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Đăng nhập Google thất bại
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();
                        
                        // Lưu thông tin user vào Firestore nếu là user mới
                        saveUserToFirestore(user);
                        
                        updateUI(user);
                    } else {
                        // Thất bại
                        Toast.makeText(LoginActivity.this, "Xác thực Firebase thất bại.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        if (user == null) return;
        
        // Kiểm tra xem user đã tồn tại trong Firestore chưa để tránh ghi đè dữ liệu cũ
        db.collection("users").document(user.getUid()).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    // Nếu chưa có thì tạo mới
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", user.getEmail());
                    userData.put("name", user.getDisplayName());
                    userData.put("avatarUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
                    userData.put("role", "customer"); // Mặc định role là customer

                    db.collection("users").document(user.getUid()).set(userData)
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                }
            });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                        updateUI(mAuth.getCurrentUser());
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
    }
}
