package com.jiowhere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AuthenticationActivity extends AppCompatActivity {
    private EditText loginEmailEt, loginPassEt, regNameEt, regEmailEt, regPassEt;
    private Button loginBtn, regBtn;
    private String name, regEmail, loginEmail, loginPass, regPass, imgURL;
    private DatabaseReference dbref;
    private FirebaseAuth mAuth;
    private CircleImageView userImg;
    private final static int GALLERY_PICK_CODE = 1;
    private Uri imageUri;
    private ProgressDialog pd;
    private StorageReference stRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        loginBtn = findViewById(R.id.login_btn);
        loginEmailEt = findViewById(R.id.login_email_et);
        loginPassEt = findViewById(R.id.login_password_et);
        regBtn = findViewById(R.id.reg_btn);
        regNameEt = findViewById(R.id.reg_name_et);
        regEmailEt = findViewById(R.id.reg_email_et);
        regPassEt = findViewById(R.id.reg_password_et);

        dbref = FirebaseDatabase.getInstance().getReference();
        stRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        userImg = findViewById(R.id.user_img);
        pd = new ProgressDialog(this);
        pd.setMessage("uploading");

        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImg();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmail = loginEmailEt.getText().toString();
                loginPass = loginPassEt.getText().toString();
                if (TextUtils.isEmpty(loginEmail)) {
                    loginEmailEt.setError("Enter email");
                } else if (TextUtils.isEmpty(loginPass)) {
                    loginPassEt.setError("Enter password");

                } else {
                    pd.setMessage("logging in");
                    pd.show();
                    login(loginEmail, loginPass);

                }
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = regNameEt.getText().toString();
                regEmail = regEmailEt.getText().toString();
                regPass = regPassEt.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    regNameEt.setError("Enter name");
                } else if (TextUtils.isEmpty(regEmail)) {
                    regEmailEt.setError("Enter email");
                } else if (TextUtils.isEmpty(regPass)) {
                    regPassEt.setError("Enter password");

                } else {
                    pd.setMessage("Creating account");
                    pd.show();
                    register(name, regEmail, regPass);

                }
            }
        });
    }

    private void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Intent main = new Intent(AuthenticationActivity.this,
                                    MainActivity.class);
                            startActivity(main);
                            finish();
                            Toast.makeText(AuthenticationActivity.this, "logged in", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AuthenticationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void register(final String name, final String email, final String password) {
        if (imageUri == null) {
            imageUri=Uri.parse("android.resource://com.jiowhere/"+R.drawable.profile);
            pd.setMessage("Creating account");
            pd.dismiss();
            //Toast.makeText(this, "Please upload an image", Toast.LENGTH_LONG).show();
        }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            pd.show();
                            if (task.isSuccessful()) {
                                
                                String userId = mAuth.getCurrentUser().getUid();
                               // Toast.makeText(AuthenticationActivity.this, userId, Toast.LENGTH_SHORT).show();
                                final StorageReference filePath = stRef.child("profile image").child(
                                        userId);

                                final UploadTask uploadTask = filePath.putFile(imageUri);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<
                                        UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot>
                                                                   task) {
                                        if (task.isSuccessful()) {
                                            pd.dismiss();
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
                                                    HashMap<String,String> userMap = new HashMap();
                                                    userMap.put("image", imgURL);
                                                    userMap.put("thumb_image", imgURL);
                                                    userMap.put("name",name);
                                                    userMap.put("email",email);
                                                    userMap.put("password",password);
                                                    userMap.put("userId",mAuth.getCurrentUser().getUid());
                                                    userMap.put("lat","32.5898866");
                                                    userMap.put("lng","72.1376698");

                                                    dbref.child("users").child(mAuth.getCurrentUser().getUid())
                                                            .setValue(userMap).addOnCompleteListener(new OnCompleteListener() {
                                                        @Override
                                                        public void onComplete(@NonNull Task task) {
                                                            Intent main=new Intent(AuthenticationActivity.this,
                                                                    MainActivity.class);
                                                            startActivity(main);
                                                            finish();
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
                                       // Toast.makeText(AuthenticationActivity.this, e.toString(),Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else{
                                Toast.makeText(AuthenticationActivity.this, "Please enter correct Email/Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            userImg.setImageURI(imageUri);
        }
    }


    public void uploadImg() {
        Intent gallery = new Intent();
        gallery.setAction(Intent.ACTION_GET_CONTENT);
        gallery.setType("image/*");
        startActivityForResult(gallery, GALLERY_PICK_CODE);

    }
}
