package dev.aftermoon.indianpoker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dev.aftermoon.indianpoker.game.GameActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Intent i = new Intent(getApplicationContext(), StartActivity.class);
        startActivity(i);
    }
}