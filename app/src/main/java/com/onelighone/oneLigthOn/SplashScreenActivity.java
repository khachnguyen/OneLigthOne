package com.onelighone.oneLigthOn;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static android.os.SystemClock.sleep;

public class SplashScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sleep(1000);
        startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        finish();
    }
}

