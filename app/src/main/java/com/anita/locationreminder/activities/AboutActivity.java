package com.anita.locationreminder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.anita.locationreminder.R;

public class AboutActivity extends AppCompatActivity {
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        imageView = findViewById(R.id.imageView);
    }

    public void Backbtn(View view) {
        super.onBackPressed();
    }

    public void btnstart(View view) {
            Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate);
            imageView.startAnimation(rotate);
    }
}
