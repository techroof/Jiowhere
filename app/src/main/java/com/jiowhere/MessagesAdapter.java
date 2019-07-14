package com.jiowhere;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>{
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    Context context;

    public MessagesAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        mAuth=FirebaseAuth.getInstance();
        String userId=null;
        // if (userId.equals(mAuth.getCurrentUser().getUid())) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder viewHolder, int i) {
        String senderId=mAuth.getCurrentUser().getUid();
        Messages messages=mMessageList.get(i);
        String fromUserId=messages.getFrom();
        String fromMessageType=messages.getType();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    // Toast.makeText(context, "img", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if (fromMessageType.equals("text")){
            viewHolder.receiver.setVisibility(View.INVISIBLE);

            if (fromUserId.equals(senderId)){
                viewHolder.sender.setTextColor(Color.BLACK);
                viewHolder.sender.setGravity(Gravity.LEFT);
                viewHolder.sender.setText(messages.getMessage());

            }
            else{
                viewHolder.sender.setVisibility(View.INVISIBLE);

                viewHolder.receiver.setVisibility(View.VISIBLE);
                //viewHolder.receiver.setTextColor(Color.WHITE);
                viewHolder.receiver.setGravity(Gravity.LEFT);
                viewHolder.receiver.setText(messages.getMessage());

            }
        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView sender,receiver;
        public MessageViewHolder(View view) {
            super(view);
            sender= (TextView) view.findViewById(R.id.sender_txt);
            receiver=(TextView) view.findViewById(R.id.receiver_txt);

        }
    }
}
