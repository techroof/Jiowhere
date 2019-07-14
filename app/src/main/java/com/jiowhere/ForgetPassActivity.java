package com.jiowhere;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ForgetPassActivity extends AppCompatActivity {
    private EditText emailET, newPassEt, confirmPassEt;
    private Button resetBtn;
    private String email, newPass, confirmPass;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pass);

        resetBtn = findViewById(R.id.reset_btn);
        emailET = findViewById(R.id.forget_email);
        newPassEt = findViewById(R.id.new_password);
        confirmPassEt = findViewById(R.id.confirm_new_pass);
        pd=new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        pd.setMessage("Resetting password");

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailET.getText().toString();
                newPass = newPassEt.getText().toString();
                confirmPass = confirmPassEt.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    emailET.setError("Enter email");
                } else if (TextUtils.isEmpty(newPass)) {
                    newPassEt.setError("Enter password");

                } else if (TextUtils.isEmpty(confirmPass)) {
                    confirmPassEt.setError("Confirm password");

                } else {
                    pd.setMessage("logging in");
                    pd.show();
                    //reset();

                }
            }
        });
    }
}
