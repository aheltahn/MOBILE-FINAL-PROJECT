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
        String imageUrl = "https://i.pinimg.com/1200x/3c/ab/31/3cab3194a659c38049c779f5addeb4bb.jpg";

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .into(imageViewHero);

        // Ảnh minh họa 1
        ImageView imageViewIntro1 = findViewById(R.id.imageViewIntro1);
        String url1 = "https://i.pinimg.com/1200x/76/dd/14/76dd14b3aeedc2f682c926e7c77cb79e.jpg";
        Picasso.get()
                .load(url1)

                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .fit()
                .centerCrop()
                .into(imageViewIntro1);

        // Ảnh minh họa 2
        ImageView imageViewIntro2 = findViewById(R.id.imageViewIntro2);
        String url2 = "https://i.pinimg.com/736x/35/61/0a/35610afc8b99c3b57e5619a9ea1c090e.jpg";

        Picasso.get()
                .load(url2)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .fit()
                .centerCrop()
                .into(imageViewIntro2);

        // Ảnh minh họa 3
        ImageView imageViewIntro3 = findViewById(R.id.imageViewIntro3);
        String url3 = "https://i.pinimg.com/736x/79/05/89/7905898d6ff9d7f541b6a99793b583c8.jpg";

        Picasso.get()
                .load(url3)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .fit()
                .centerCrop()
                .into(imageViewIntro3);

        // Ảnh minh họa 4
        ImageView imageViewIntro4 = findViewById(R.id.imageViewIntro4);
        String url4 = "https://i.pinimg.com/736x/47/ac/c2/47acc240d4328a40cf03bd4f95d8ac37.jpg";

        Picasso.get()
                .load(url4)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .fit()
                .centerCrop()
                .into(imageViewIntro4);

        // Ảnh welcome mới
        ImageView imageViewWelcome = findViewById(R.id.imageViewWelcome);
        String welcomeUrl = "https://i.pinimg.com/736x/b0/fc/a4/b0fca4bff17141f661c9ae5d1892dc11.jpg";

        Picasso.get()
                .load(welcomeUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher_round)
                .fit()
                .centerCrop()
                .into(imageViewWelcome);

    }
}
