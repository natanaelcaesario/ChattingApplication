package com.example.chattingapplication;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private CircleImageView mProfilImage;
    private Button mProfileSendReqBtn, mProfileDeclineReqBtn;
    // Database
    private DatabaseReference mUsersDatabase;   //users
    private DatabaseReference mFriendRequestDatabase; //friend request
    private DatabaseReference mFriendDatabase; // friends
    private DatabaseReference mNotificationDatabase;
    //Firebase auth
    private FirebaseUser mCurrentUser;
    // progress dialog
    private ProgressDialog mProgressDialog;
    //Toolbar
    private Toolbar mToolbar;

    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Database
        final String user_id = getIntent().getStringExtra("user_id");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        //Current user auth
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        //var
        mProfilImage = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_display_name);
        mProfileStatus = findViewById(R.id.profile_status);
//        mProfileFriendsCount = findViewById(R.id.profile_total_friends);
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn);
        mProfileDeclineReqBtn = findViewById(R.id.decline_button);
        //starting state
        current_state = "not_friends";
        //progressing user
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Pleae wait while we load the user information");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        //toolbar
        mToolbar = findViewById(R.id.profile_toolbar);
        //dbase add value
        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                //load name
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);
                //load image
                Picasso.get().load(image).placeholder(R.drawable.defaultavatar).into(mProfilImage);
                // friends feature
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")) {
                                current_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mProfileDeclineReqBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineReqBtn.setEnabled(true);
                                mProgressDialog.dismiss();
                            } else if (req_type.equals("sent")) {
                                current_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        }

                        // KALAU GA ADA BRARTI UDAH TEMENAN
                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)) {
                                        current_state = "friends";
                                        mProfileSendReqBtn.setText("UnFriend This Person");
                                    }
                                    mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineReqBtn.setEnabled(false);
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
                setSupportActionBar(mToolbar);
                getSupportActionBar().setTitle(display_name + " Profil's");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(true);

                //-- NOT FRIEND STATEE
                // / SEND FRIEND
                if (current_state.equals("not_friends")) {
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                HashMap<String, String> notificationData = new HashMap<>();
                                                notificationData.put("from", mCurrentUser.getUid());
                                                notificationData.put("type", "request");
                                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                mProfileDeclineReqBtn.setEnabled(false);
                                                mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        current_state = "req_sent";
                                                        Toast.makeText(ProfileActivity.this, "Request Sent.", Toast.LENGTH_SHORT).show();
                                                        mProfileSendReqBtn.setText("Cancel Friend Request");
                                                        mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                        mProfileDeclineReqBtn.setEnabled(false);
                                                    }
                                                });
                                            }
                                        });
                                    } else {
                                        Toast.makeText(ProfileActivity.this, "Failed Sending Your Request.", Toast.LENGTH_SHORT).show();
                                    }
                                    mProfileSendReqBtn.setEnabled(true);
                                }
                            });
                }

                //CANCEL REQUEST STATE .. HAPUS REQUEST PERTEMANAN
                if (current_state.equals("req_sent")) {
                    //deleting single value form database
                 mNotificationDatabase.child(user_id).orderByKey().equalTo(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         String key = dataSnapshot.getKey();
                         dataSnapshot.getRef().removeValue(); }
                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {
                     }});
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    current_state = "not_friends";
                                    Toast.makeText(ProfileActivity.this, "Request Canceled.", Toast.LENGTH_SHORT).show();
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                    mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                    mProfileDeclineReqBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }
                // ACCEPTED FRIEND REQUEST
                if (current_state.equals("req_received")) {
                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    current_state = "friends";
                                                    mProfileSendReqBtn.setText("Unfriend this person");
                                                    Toast.makeText(ProfileActivity.this, "This person is now your friend", Toast.LENGTH_SHORT).show();
                                                    mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                                    mProfileDeclineReqBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                // remove friend .. HAPUS PERTEMANAN
                if (current_state.equals("friends")) {
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    current_state = "not_friends";
                                    Toast.makeText(ProfileActivity.this, "Removed From Your Friend List", Toast.LENGTH_SHORT).show();
                                    mProfileSendReqBtn.setText("Send Friend Request");
                                }
                            });
                        }
                    });
                }
            }
        });

        // TOLAK PERTEMANAN
        mProfileDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mProfileSendReqBtn.setEnabled(true);
                                current_state = "not_friends";
                                Toast.makeText(ProfileActivity.this, "Request Declined", Toast.LENGTH_SHORT).show();
                                mProfileSendReqBtn.setText("Send Friend Request");
                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);
                            }
                        });
                    }
                });
            }
        });

    }
}
