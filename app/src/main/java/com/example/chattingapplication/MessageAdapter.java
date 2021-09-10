package com.example.chattingapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

//geting data from console and store it into the list view
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private FirebaseAuth mAuth;
    private List<Messages> mMassageList;
    private DatabaseReference User_detail;
    private CircleImageView profileimage;


    MessageAdapter(List<Messages> mMassageList){
        this.mMassageList = mMassageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,
                parent, false);
        return new MessageViewHolder(v);
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView displayText;
        CircleImageView profileimage;
        ImageView messageImage;

        MessageViewHolder(@NonNull View view) {
            super(view);
            messageText = view.findViewById(R.id.message_text_layout);
            displayText = view.findViewById(R.id.message_text_display_name);
            profileimage = view.findViewById(R.id.message_profil_layout);
            messageImage = view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder viewHolder, int i){
        mAuth = FirebaseAuth.getInstance();
        Messages c = mMassageList.get(i);
        final String from_user = c.getFrom();
        String message_type = c.getType();
        final String current_user_id = mAuth.getCurrentUser().getUid();

        User_detail = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        User_detail.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                viewHolder.displayText.setText(name);
             //  Picasso.get().load(image).placeholder(R.drawable.defaultavatar).into(profileimage);
                Picasso.get().load(image).placeholder(R.drawable.defaultavatar).into(viewHolder.profileimage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //costumize layout of the message
       if(message_type.equals("text")){
            //viewHolder.messageText.setBackgroundColor(Color.WHITE);
            //viewHolder.messageText.setTextColor(Color.BLACK);
           viewHolder.messageText.setText(c.getMessage());
           viewHolder.messageImage.setVisibility(View.INVISIBLE);
        }
        else{
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.get().load(c.getMessage()).placeholder(R.drawable.defaultavatar).into(viewHolder.messageImage);
            //viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            //viewHolder.messageText.setTextColor(Color.WHITE);
        }

        viewHolder.messageText.setText(c.getMessage());

    }
    @Override
    public int getItemCount(){

        return mMassageList.size();
    }
}
