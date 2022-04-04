package com.example.notesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    long animation = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        imageView = findViewById(R.id.iv_splash);
        textView = findViewById(R.id.tv_splash);

        ObjectAnimator animatory = ObjectAnimator.ofFloat(imageView,"y",500f);
        ObjectAnimator animatorx = ObjectAnimator.ofFloat(imageView,"x",350f);

        animatory.setDuration(animation);
        animatorx.setDuration(animation);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animatory,animatorx);
        animatorSet.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },1000);

    }
}