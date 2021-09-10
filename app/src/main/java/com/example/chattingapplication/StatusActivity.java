package com.example.chattingapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    //PROGRESS
    private ProgressDialog mProgress;

    //ITEM
    private Toolbar mToolBar;
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    // FIREBASE
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);


        /// FIREBASE
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        // TOOLBAR STATUS SETTINGS
        mToolBar = findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Status Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //PROGRESS DIALOG

        //ITEM
        mStatus = findViewById(R.id.status);
        mSaveBtn = findViewById(R.id.save_btn);



        // SAVE BUTTON ACTION
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Progress
                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save your changes");
                mProgress.show();

                //METHOD TO RUN
                String status = mStatus.getEditText().getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.dismiss();
                            Toast.makeText(StatusActivity.this, "Your status Changed", Toast.LENGTH_SHORT).show();
                            Intent status = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(status);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "There is some error while you are saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }
}
