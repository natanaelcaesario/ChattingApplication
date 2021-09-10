package com.example.chattingapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    //geting user
    private FirebaseAuth mAuth;
    //creating string to store it
    private String mCurrent_user_id;
    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    mMainView = inflater.inflate(R.layout.fragment_friends, container, false);
    mFriendsList = (RecyclerView) mMainView.findViewById(R.id.friends_list);
    mAuth = FirebaseAuth.getInstance();
    mCurrent_user_id = mAuth.getCurrentUser().getUid()  ;
    mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
    mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    mFriendsList.setHasFixedSize(true);
    mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendRecycleViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ){
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {
                final String list_user_id = getRef(position).getKey();   // i is the position of the current list item
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String status = dataSnapshot.child("status").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                      //geting status
                       if(dataSnapshot.hasChild("online")){
                           String userOnline = dataSnapshot.child("online").getValue().toString();
                           friendsViewHolder.setUserOnline(userOnline);
                       }
                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setStatus(status);
                        friendsViewHolder.setUserImage(thumb_image, getContext());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profil", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0){
                                            Intent profilIntent = new Intent(getContext(), ProfileActivity.class);
                                            profilIntent.putExtra("user_id", list_user_id);
                                            startActivity(profilIntent);
                                        }
                                        if(which == 1){
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            //bring the extra to the chat intent
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            chatIntent.putExtra("status", status);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriendsList.setAdapter(friendRecycleViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
    public FriendsViewHolder(View itemView){
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

    public void setUserOnline(String online_status){
        ImageView userOnlineView = (ImageView) mView.findViewById(R.id.users_single_online);
        if(online_status.equals("true")){
            userOnlineView.setVisibility(View.VISIBLE);
        }
        else{
            userOnlineView.setVisibility(View.INVISIBLE);
        }

    }

    }
}
