package com.jiowhere;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {
    private Context context;
    private DatabaseReference mref;
    private FirebaseAuth mAuth;
    private ArrayList<Friends> friends;
    private String userPhoneKey, suggestState = "unsuggested";
    private String id, lat, lng;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    private String userID;
    private ProgressDialog pd;

    public FriendsAdapter(Context context, ArrayList<Friends> friends) {
        this.context = context;
        this.friends = friends;
        mref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(context);
        pd.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout, parent, false);
        FriendsViewHolder viewHolder = new FriendsViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsViewHolder holder, final int position) {
        Picasso.get().load(friends.get(position).getImage()).into(holder.userImg);
        holder.userName.setText(friends.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                id = friends.get(position).getUserId();
                mref.child(mAuth.getCurrentUser().getUid()).child("suggested")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    lat = dataSnapshot.child("lat").getValue().toString();
                                    lng = dataSnapshot.child("lng").getValue().toString();

                                    Map<String, Object> sug = new HashMap<>();
                                    sug.put("lat", lat);
                                    sug.put("lng", lng);
                                    if (suggestState.equals("suggested")) {
                                        pd.setMessage("Unsuggesting Location");
                                        pd.show();
                                        if (dataSnapshot.exists()) {
                                            mref.child("users").child(friends.get(position).getUserId())
                                                    .child("suggested").removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            suggestState = "unsuggested";
                                                            pd.dismiss();
                                                            Toast.makeText(context, "Location unsuggested to this friend", Toast.LENGTH_SHORT).show();

                                                        }
                                                    });
                                        }
                                    } else if (suggestState.equals("unsuggested")) {
                                        pd.setMessage("Suggesting Location");
                                        pd.show();
                                        mref.child("users").child(friends.get(position).getUserId())
                                                .child("suggested").updateChildren(sug).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                suggestState = "suggested";
                                                pd.dismiss();
                                                Toast.makeText(context, "Location suggested to this friend", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }
        });         // holder.userDistance.setText(friends.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        ImageView userImg;
        TextView userName;//userDistance;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            userImg = itemView.findViewById(R.id.user_single_image);
            userName = itemView.findViewById(R.id.user_single_name);
            //userDistance = itemView.findViewById(R.id.user_single_status);
        }
    }
}
