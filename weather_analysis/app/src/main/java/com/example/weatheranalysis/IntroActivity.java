package com.example.weatheranalysis;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.example.weatheranalysis.classes.util.GeneralUtils;
//activity του intro
public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


    }

    public void start(View view){
        SharedPreferences preferences = getSharedPreferences("intro", Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean("isFirstTime", false).apply();
        GeneralUtils.goToActivity(this, LoginActivity.class);
    }
}