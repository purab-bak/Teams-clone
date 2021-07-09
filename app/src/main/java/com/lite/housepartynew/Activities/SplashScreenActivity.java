package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.load.data.FileDescriptorAssetPathFetcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lite.housepartynew.R;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView logo;
    private static int SPLASH_TIME_OUT = 2850;

    Animation splashAnim;

    FirebaseUser mCurrentUser = null;

    Intent i;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logo = findViewById(R.id.splashLogo);

        splashAnim = AnimationUtils.loadAnimation(this, R.anim.splash_anim);

        logo.setAnimation(splashAnim);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (mCurrentUser != null) {
                    i = new Intent(SplashScreenActivity.this, DashboardActivity.class);
                } else {
                    i = new Intent(SplashScreenActivity.this, LoginActivity.class);
                }
                startActivity(i);
                finish();
            }

        }, SPLASH_TIME_OUT);

    }
}