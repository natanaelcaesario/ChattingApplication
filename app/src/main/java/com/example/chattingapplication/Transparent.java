package com.example.chattingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Transparent extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.layout);
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                {
                    Intent allusersIntent = new Intent(Transparent.this, MainActivity.class);
                    startActivity(allusersIntent);

                }
            }
        });
    }


}
