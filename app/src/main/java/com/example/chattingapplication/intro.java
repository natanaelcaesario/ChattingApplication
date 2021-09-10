package com.example.chattingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class intro extends AppCompatActivity {

    private ViewPager screenPager;
    SplashScreenPagerAdapter SplashScreenPagerAdapter;
    TabLayout tabIndicator;
    private Button mButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //request fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_intro);
        //hide action bar
        mButton = findViewById(R.id.next_btn);
        tabIndicator = findViewById(R.id.tab_indicator);

        List<SplashScreenItem> list = new ArrayList<>();

        list.add(new SplashScreenItem("Gunadarma Messenger", "Start Chatting And Get Some Friend",R.drawable.chattting));
        list.add(new SplashScreenItem("Gunadarma Messenger", "Connecting You Directly To Others", R.drawable.chatting2));

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent = new Intent(intro.this, StartActivity.class);
                startActivity(reg_intent);
            }
        });

        screenPager = findViewById(R.id.screenviewpager);
        SplashScreenPagerAdapter = new SplashScreenPagerAdapter(this, list);
        screenPager.setAdapter(SplashScreenPagerAdapter);

        tabIndicator.setupWithViewPager(screenPager);

    }
}
