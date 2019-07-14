package com.jiowhere;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private PagerAdapter pagerAdapter;
    private Toolbar toolbar;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private DatabaseReference dbref;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.main_toolbar);
        mViewPager = findViewById(R.id.main_view_pager);
        mTabLayout = findViewById(R.id.main_tabs);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("JioWhere");
        mAuth = FirebaseAuth.getInstance();

        try {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
        } catch (Exception e) {
            // Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        if (mAuth.getCurrentUser() != null) {
            dbref = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mAuth.getCurrentUser().getUid());

        }

        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if (item.getItemId()==R.id.main_logout_btn){
             FirebaseAuth.getInstance().signOut();
             Intent start=new Intent(MainActivity.this,
                     StartActivity.class);
             startActivity(start);
         }else if (item.getItemId()==R.id.settings){
             Intent settings=new Intent(MainActivity.this,
                     SettingsActivity.class);
             startActivity(settings);

         }
         return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent start=new Intent(MainActivity.this,
                    StartActivity.class);
            startActivity(start);
        }else{
            dbref.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
          dbref.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
