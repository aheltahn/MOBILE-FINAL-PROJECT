package com.example.finalprojectmu.fishiohouse;

import android.app.Application;
import com.google.firebase.FirebaseApp;

// Chúng ta sẽ vô hiệu hóa hoàn toàn App Check để giải quyết lỗi

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Chỉ khởi tạo Firebase như bình thường
        FirebaseApp.initializeApp(this);
    }
}
