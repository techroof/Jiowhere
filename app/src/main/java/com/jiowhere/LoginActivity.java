package com.jiowhere;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private TextView regText, forgetPassText;
    private Button loginBtn;
    private EditText emailET, passET;
    private DatabaseReference dbref;
    private FirebaseAuth mAuth;
    private String email, password, imgURL;
    private Uri imageUri;
    private ProgressDialog pd;
    private ImageView gmailBtn, fbBtn;
    private final static int RC_SIGN_IN = 1;
    private GoogleSignInClient googleSignInClient;
    private StorageReference stRef;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        regText = findViewById(R.id.create_account_text);
        loginBtn = findViewById(R.id.login_btn);
        emailET = findViewById(R.id.login_email_et);
        passET = findViewById(R.id.login_pass_et);
        gmailBtn = findViewById(R.id.gmail_login);
       // fbBtn = findViewById(R.id.facebook_login);
        stRef = FirebaseStorage.getInstance().getReference();

        //forgetPassText=findViewById(R.id.forget_password_text);
        dbref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Logging in");

        GoogleSignInOptions geo = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, geo);


        gmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailET.getText().toString();
                password = passET.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    emailET.setError("Enter email");
                } else if (TextUtils.isEmpty(password)) {
                    passET.setError("Enter password");

                } else {
                    pd.setMessage("logging in");
                    pd.show();
                    login(email, password);

                }
            }
        });
        regText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(register);
            }
        });
    }

    private void signIn() {
        Intent signIn = googleSignInClient.getSignInIntent();
        startActivityForResult(signIn, RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    FirebaseAccountWithGoogle(account);
                }

            } catch (Exception e) {

            }
        }
    }

    private void FirebaseAccountWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        for (UserInfo profile : user.getProviderData()) {
                            final String userid = profile.getUid();
                            final String email = profile.getEmail();
                            final String name = profile.getDisplayName();

                            dbref.child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.child(userid).exists()) {
                                        String userId = userid;
                                        imageUri= Uri.parse("android.resource://com.jiowhere/"+R.drawable.profile);

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
                                                            userMap.put("userId",userid);
                                                            userMap.put("lat","32.5898866");
                                                            userMap.put("lng","72.1376698");

                                                            dbref.child("users").child(userid)
                                                                    .setValue(userMap).addOnCompleteListener(new OnCompleteListener() {
                                                                @Override
                                                                public void onComplete(@NonNull Task task) {
                                                                    Intent main=new Intent(LoginActivity.this,
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
                                        Intent main=new Intent(LoginActivity.this,MainActivity.class);
                                        startActivity(main);
                                    }
                                    }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Intent main = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            startActivity(main);
                            finish();
                            Toast.makeText(LoginActivity.this, "logged in", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
