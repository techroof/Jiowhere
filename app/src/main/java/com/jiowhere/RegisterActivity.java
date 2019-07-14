package com.jiowhere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameET,emailEt,passET;
    private Button registerBtn;
    private TextView loginText;
    private String name,regEmail,regPass,imgURL;
    private FirebaseAuth mAuth;
    private StorageReference stRef;
    private DatabaseReference dbref;
    private ProgressDialog pd;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameET=findViewById(R.id.reg_name_et);
        emailEt=findViewById(R.id.reg_email_et);
        passET=findViewById(R.id.reg_pass_et);
        registerBtn=findViewById(R.id.register_btn);
        loginText=findViewById(R.id.already_account_text);
        dbref= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        stRef = FirebaseStorage.getInstance().getReference();

        pd=new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameET.getText().toString();
                regEmail = emailEt.getText().toString();
                regPass = passET.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    nameET.setError("Enter name");
                } else if (TextUtils.isEmpty(regEmail)) {
                    emailEt.setError("Enter email");
                } else if (TextUtils.isEmpty(regPass)) {
                    passET.setError("Enter password");

                } else {
                    pd.setMessage("Creating account");
                    pd.show();
                    register(name, regEmail, regPass);

                }
            }
        });
        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent login=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(login);
            }
        });

    }

    private void register(final String name, final String email, final String password) {
        if (imageUri == null) {
            imageUri= Uri.parse("android.resource://com.jiowhere/"+R.drawable.profile);
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
                                                        Intent main=new Intent(RegisterActivity.this,
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
                            Toast.makeText(RegisterActivity.this, "Please enter correct Email/Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
