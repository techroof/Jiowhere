package com.jiowhere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private Button changeImgBtn;
    private TextView userNameTv;
    private DatabaseReference dbref;
    private FirebaseAuth mAuth;
    private final static int GALLERY_PICK_CODE = 1;
    private Uri imageUri;
    private ImageView userImg;
    private ProgressDialog pd;
    private String imgURL,name,img;
    private StorageReference stRef;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.settings_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        changeImgBtn = findViewById(R.id.upload_img_btn);
        userNameTv = findViewById(R.id.settings_username);
        mAuth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference();
        userImg = findViewById(R.id.profile_image);
        pd = new ProgressDialog(this);
        stRef = FirebaseStorage.getInstance().getReference();

        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImg();
            }
        });
        changeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeImg();
            }
        });

        dbref.child("users").child(mAuth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        name=dataSnapshot.child("name").getValue().toString();
                        img=dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(img).into(userImg);
                        userNameTv.setText(name);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void UploadImg() {
        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, GALLERY_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            userImg.setImageURI(imageUri);
        }
    }

    public void storeImg() {
        if (imageUri == null) {
            pd.dismiss();
            Toast.makeText(this, "Please upload an image", Toast.LENGTH_LONG).show();
        } else {
            pd.show();
            final StorageReference filePath = stRef.child("profile image")
                    .child(mAuth.getCurrentUser().getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);
            uploadTask.addOnCompleteListener(new OnCompleteListener<
                    UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        pd.dismiss();
                        Toast.makeText(SettingsActivity.this,
                                "Uploaded Successfully", Toast.LENGTH_LONG).show();
                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot,
                                Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (task.isSuccessful()) {
                                    pd.dismiss();
                                    imgURL = filePath.getDownloadUrl().toString();
                                    return filePath.getDownloadUrl();
                                } else {
                                    pd.dismiss();
                                    throw task.getException();
                                }
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                pd.dismiss();
                                imgURL = task.getResult().toString();
                                Map<String, Object> userMap = new HashMap();
                                userMap.put("image", imgURL);
                                userMap.put("thumb_image", imgURL);
                                dbref.child("users").child(mAuth.getCurrentUser().getUid())
                                        .updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        Intent main = new Intent(
                                                SettingsActivity.this,
                                               MainActivity.class);
                                        startActivity(main);
                                    }
                                });
                            }
                        });
                    }
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred())
                            / taskSnapshot.getTotalByteCount();
                    pd.setMessage((int) progress + "% Uploaded");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(SettingsActivity.this, e.toString(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent main = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(main);
        }
        return super.onOptionsItemSelected(item);
    }

}
