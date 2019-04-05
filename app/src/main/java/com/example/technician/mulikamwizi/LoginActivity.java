package com.example.technician.mulikamwizi;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private Button btnLogin,btnReset;
    private TextView link_toregister;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // geting firebase auth instance
        auth =FirebaseAuth.getInstance();
        if (auth.getCurrentUser() !=null){
            startActivity(new Intent(LoginActivity.this,Home.class));
            finish();
        }

        setContentView(R.layout.login);
        inputEmail= findViewById(R.id.email);
        inputPassword= findViewById(R.id.password);
        progressBar= findViewById(R.id.progressBar);
        btnLogin= findViewById(R.id.btn_Login);
        btnReset= findViewById(R.id.btn_reset_password);

        TextView registerScreen = findViewById(R.id.link_to_register);
        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
        //resetting password
                btnReset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
                    }
                });

                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email=inputEmail.getText().toString();
                        final String password=inputPassword.getText().toString();
                        //optional
                        if (TextUtils.isEmpty(email)){
                            Toast.makeText(getApplicationContext(),"enter email address",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (TextUtils.isEmpty(password)){
                            Toast.makeText(getApplicationContext(), "enter password", Toast.LENGTH_LONG).show();
                            return;
                        }
                        progressBar.setVisibility(View.VISIBLE);
                        //authentication
                        auth.signInWithEmailAndPassword(email,password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if (!task.isSuccessful()){
                                            if (password.length()<6){
                                                inputPassword.setError(getString(R.string.minimum_password));
                                            }else {
                                                Toast.makeText(LoginActivity.this,getString(R.string.auth_failed),Toast.LENGTH_LONG).show();
                                            }
                                        }else{
                                            Intent intent =new Intent(LoginActivity.this,Home.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                });
                    }
                });

               //new stuff
               /* TextView protectScreen =(TextView)findViewById(R.id.btnLogin);
                protectScreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Intent  z = new Intent(getApplicationContext(),protect.class);
                        startActivity(z);
                    }
                });
                */
                //end of new stuff
            }
        });
    }
}
