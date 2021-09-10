package com.example.chattingapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    //string
    private String userName;
    private String status;
    private String mChatUser;
    private String mCurrentUserId;
    //toolbar
    private Toolbar mChatToolBar;
    //database
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    //pellete
    private TextView mNameView;
    private TextView mStatusView;
    private CircleImageView mProfileImage;
    private EditText mChatMessageView;
    private Button mChatSendBtn;
    private RecyclerView mMessageList;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mCurrentUserDisplayName;
    private ImageButton mChatAddBtn;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;


    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private static final int GALLERY_PICK = 1;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolBar = (Toolbar) findViewById(R.id.chat_app_bar);
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);
        mChatSendBtn = (Button) findViewById(R.id.chat_send_btn);
//        mChatAddBtn = (ImageButton) findViewById(R.id.chat_add_btn);
        mAdapter = new MessageAdapter(messagesList);
        mMessageList = (RecyclerView) findViewById(R.id.messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_message_layout);
        mLinearLayout = new LinearLayoutManager(this);
        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);
        mMessageList.setAdapter(mAdapter);

        //toolbar
        setSupportActionBar(mChatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //database and others
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //getting extra from friends fragment
        mChatUser = getIntent().getStringExtra("user_id");

        userName = getIntent().getStringExtra("user_name");
        status = getIntent().getStringExtra("status");

        //including chat custom bar
        LayoutInflater inflater =  (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        //custom bar items
        mNameView = (TextView) findViewById(R.id.custom_bar_dname);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mStatusView = (TextView) findViewById(R.id.custom_bar_status);
        mCurrentUserDisplayName = (TextView) findViewById(R.id.message_text_display_name);
        //TITLE
        mStatusView.setText(status);
        mNameView.setText(userName);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        loadMessages();
        readMessages();
        //new var

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //geting datasnapshot value from database
            String online = dataSnapshot.child("online").getValue().toString();
            String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
            String status = dataSnapshot.child("status").getValue().toString();
            //set title
            //mStatusView.setText(status);
            //last seen feature
                Picasso.get().load(thumb_image).placeholder(R.drawable.defaultavatar).into(mProfileImage);

             /*   if(online.equals("true")){
               /    mStatusView.setText("Online");
                    }
                else{
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);
                    String lastseentime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mStatusView.setText(lastseentime);
                    } */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check into the chat database , if the current user doesnt have the value , then create one
                if(!dataSnapshot.hasChild(mChatUser)){
                    //chat value
                    Map chatAddMap =  new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put ("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put ("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
      /*  mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galeryIntent = new Intent();
                galeryIntent.setType("image/*");
                galeryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galeryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        }); */


        //load page so user dont have to load so much , like facebook
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });

    }

    private void readMessages() {
        DatabaseReference messageRef =  mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        messageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Messages seen = dataSnapshot.getValue(Messages.class);
                if("seen".equals(false)){
                    seen.setSeen(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final String current_user_ref ="messages/"+mCurrentUserId + "/" + mChatUser;
            final String chat_user_ref = "messages/" + mChatUser + "/" +mCurrentUserId;
            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push();
            final String push_id = user_message_push.getKey();
            StorageReference filepath = mStorageRef.child("messages_images").child(push_id + ".jpg");


            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String download_url  = uri.toString();
                            final String thumb_downloadUrl = uri.toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message",download_url);
                            messageMap.put("seen", false);
                            messageMap.put("type", "image");
                            messageMap.put("time", ServerValue.TIMESTAMP);
                            messageMap.put("from", mCurrentUserId);

                            Map messageUserMap = new HashMap();
                            messageUserMap.put(current_user_ref + "/" + push_id , messageMap);
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
                            mChatMessageView.setText("");
                            mRootRef.updateChildren(messageUserMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    Intent chatIntent = new Intent(ChatActivity.this, ChatActivity.class);
                                    chatIntent.putExtra("user_id", mChatUser);
                                    chatIntent.putExtra("user_name", userName);
                                    chatIntent.putExtra("status", status);
                                    startActivity(chatIntent);
                                    Toast.makeText(ChatActivity.this, "Your image was sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });

        }

    }

    private void loadMoreMessages(){
        DatabaseReference messageRef =  mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                }else{
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1 ){
                    mLastKey = messageKey;
                }

                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {
        final DatabaseReference messageRef =  mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1 ){
                        String messageKey = dataSnapshot.getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;
                }

                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessageList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage() {
        //geting the chat message value , convert to string
    String message = mChatMessageView.getText().toString();
    if(!TextUtils.isEmpty(message)){
        //mChatUser is other user , mCurrentUserId is the current user that sending message
        String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
        String chat_user_ref ="messages/" + mChatUser + "/" + mCurrentUserId;

        DatabaseReference user_message_push = mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).push();

        String push_id  = user_message_push.getKey();

        Map messageMap = new HashMap();
        messageMap.put( "message" , message);
        messageMap.put("seen", false);
        messageMap.put("type", "text");
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("from", mCurrentUserId);

        Map messageUserMap = new HashMap();
        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);
        mChatMessageView.setText("");

        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError != null){
                    Log.d("CHAT_LOG", databaseError.getMessage().toString());
                }
            }
        });
    }
    }


}
