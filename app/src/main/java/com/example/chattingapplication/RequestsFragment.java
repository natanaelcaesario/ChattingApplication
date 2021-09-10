package com.example.chattingapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private RecyclerView mFriendRequestList;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    //geting user
    private FirebaseAuth mAuth;
    //creating string to store it
    private String mCurrent_user_id;
    private View mRequestView;

    public RequestsFragment() {

        // Required empty public constructor

    }


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRequestView = inflater.inflate(R.layout.fragment_requests, container, false);
        mFriendRequestList = (RecyclerView) mRequestView.findViewById(R.id.friends_request);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(mCurrent_user_id);
        mFriendRequestList.setHasFixedSize(true);
        mFriendRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


        // Inflate the layout for this fragment
        return mRequestView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Request , RequestViewHolder> requestRecycleViewAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                Request.class,
                R.layout.users_single_layout,
                RequestsFragment.RequestViewHolder.class,
                mFriendRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder requestViewHolder, Request request, int position) {
                final String user_id = getRef(position).getKey();

                mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("name").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                        requestViewHolder.setName(userName);
                        requestViewHolder.setStatus(status);
                        requestViewHolder.setUserImage(thumb_image, getContext());
                        requestViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent request =new Intent(getContext(),ProfileActivity.class);
                                request.putExtra("user_id", user_id);
                                startActivity(request);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



            }
        };
        mFriendRequestList.setAdapter(requestRecycleViewAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public RequestViewHolder (View itemView){
            super(itemView);
            mView = itemView;
        }
        public void setStatus(String status){
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_Image, Context ctx){
            CircleImageView UserImageView = mView.findViewById(R.id.users_single_image);
            Picasso.get().load(thumb_Image).placeholder(R.drawable.defaultavatar).into(UserImageView);
        }

    }
}
