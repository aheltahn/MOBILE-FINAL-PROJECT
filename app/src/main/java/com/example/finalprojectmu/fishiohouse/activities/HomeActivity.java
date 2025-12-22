package com.example.finalprojectmu.fishiohouse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.Nullable;

import com.example.finalprojectmu.R;
import com.squareup.picasso.Picasso;

// SỬA Ở ĐÂY: Thừa kế từ BaseActivity
public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button buttonViewMenu = findViewById(R.id.buttonViewMenu);
        buttonViewMenu.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });

        ImageView imageViewHero = findViewById(R.id.imageViewHero);
        String imageUrl = "https://i.pinimg.com/1200x/68/40/c5/6840c57b2ee591984263027c3f460cf2.jpg";

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .into(imageViewHero);
    }
}
