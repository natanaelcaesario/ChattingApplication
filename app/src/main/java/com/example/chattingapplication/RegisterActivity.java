package com.example.chattingapplication;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import java.util.HashMap;
public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;
    private ProgressDialog mRegProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference tokenRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRegProgress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mDisplayName = findViewById(R.id.reg_display_name);
        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password_login);
        mCreateBtn = findViewById(R.id.reg_create_btn);
        mCreateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                if(!email.contains("@student.gunadarma.ac.id")){
                    Toast.makeText(RegisterActivity.this, "Gunakan email GUNADAMRA untuk menggunakan aplikasi!", Toast.LENGTH_SHORT).show();
                }
                else {
                    String password = mPassword.getEditText().getText().toString();
                    if (!TextUtils.isEmpty(display_name)  && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                        mRegProgress.setTitle("Registering User");
                        mRegProgress.setMessage("Please While We Creating Your Account!");
                        mRegProgress.setCanceledOnTouchOutside(false);
                        mRegProgress.show();
                        register_user(display_name, email, password);
                    }
                }

            }
        });
    }
    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", display_name);
                            userMap.put("status","Hi, There i'm using Natan 				ChatApp");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        tokenRef = FirebaseDatabase.getInstance().getReference().child("Users");
                                        mRegProgress.dismiss();
                                        String current_user_id = mAuth.getCurrentUser().getUid();
                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        Log.d("TAG","THE TOKEN REFRESHED: " + deviceToken);
                                        tokenRef.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent mainIntent = new Intent(RegisterActivity.this, Transparent.class);                                              	mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 	Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                Toast.makeText(RegisterActivity.this, "Account Created", 	Toast.LENGTH_SHORT).show();
                                                startActivity(mainIntent);
                                            }
                                        });
                                        finish();
                                    }
                                }
                            });
                        }
                        else {
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Your Registration Haven't Completed", Toast.LENGTH_LONG).show();
                        }
                    }
                });}}
