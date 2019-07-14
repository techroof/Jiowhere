package com.jiowhere;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView friendsRv;
    private FriendsAdapter adapter;
    private ArrayList<Friends> list;
    private RecyclerView.LayoutManager layoutManager;
    private FirebaseAuth mAuth;
    private String lat, lng;
    private DatabaseReference dbref;
    private Toolbar mFriendsToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendsRv = findViewById(R.id.friends_rv);
        friendsRv.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        friendsRv.setLayoutManager(layoutManager);

        mFriendsToolbar = (Toolbar) findViewById(R.id.friends_app_bar);
        setSupportActionBar(mFriendsToolbar);
        getSupportActionBar().setTitle("");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        list = new ArrayList<Friends>();
        dbref = FirebaseDatabase.getInstance().getReference();
        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");

        mAuth=FirebaseAuth.getInstance();
        dbref.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Friends f = dataSnapshot1.getValue(Friends.class);
                    list.add(f);
                }
                adapter = new FriendsAdapter(FriendsActivity.this, list);
                friendsRv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Map<String,Object> suggest=new HashMap<>();
        suggest.put("lat",lat);
        suggest.put("lng",lng);
        dbref.child(mAuth.getCurrentUser().getUid()).child("suggested").updateChildren(suggest);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent main = new Intent(FriendsActivity.this, MainActivity.class);
            startActivity(main);
        }
        return super.onOptionsItemSelected(item);
    }
}

