package com.example.technician.mulikamwizi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.BinderThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.technician.mulikamwizi.activity.MyRecs;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import io.paperdb.Paper;

public class RegisterActivity extends Activity {
    private EditText inputEmail,inputPassword,inputconfirmPass,inputPhoneno,inputName;
    private Button btnRegister;
    private TextView txtLink_To_Login;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    SharedPreferences sharedPref;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);
        //new stuff
        auth=FirebaseAuth.getInstance();
        btnRegister= findViewById(R.id.btnRegister);
        inputEmail= findViewById(R.id.reg_email);
        inputName= findViewById(R.id.reg_fullname);
        inputPhoneno= findViewById(R.id.reg_phoneno);
        inputPassword= findViewById(R.id.reg_password);
        inputconfirmPass= findViewById(R.id.reg_confirmpassword);
        txtLink_To_Login= findViewById(R.id.link_to_login);
        progressBar= findViewById(R.id.progressBar);

        Paper.init(this);

     btnRegister.setOnClickListener(new View.OnClickListener() {
         class onCompleteListener<T> implements com.google.android.gms.tasks.OnCompleteListener<AuthResult> {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {

             }
         }

         @Override
         public void onClick(View view) {
             //Save name ,email and phone number to shared preferences.
             sharedPref = getSharedPreferences("registerInfo", Context.MODE_PRIVATE);
             SharedPreferences.Editor editor=sharedPref.edit();
             editor.putString("name", inputName.getText().toString());
             editor.putString("email",inputEmail.getText().toString());
             editor.putString("phone",inputPhoneno.getText().toString());
             editor.apply();
             Toast.makeText(RegisterActivity.this,"data written to shared preference! ",Toast.LENGTH_LONG).show();


             String email=inputEmail.getText().toString().trim();
             String password=inputPassword.getText().toString().trim();
             //added stuff on monday
             if (TextUtils.isEmpty(email)){
                 Toast.makeText(getApplicationContext(),"enter email address",Toast.LENGTH_LONG).show();
                 return;
             }
             if (TextUtils.isEmpty(password)){
                 Toast.makeText(getApplicationContext(),"enter password",Toast.LENGTH_LONG).show();
                         return;
             }
             if (password.length()<6){
                 Toast.makeText(getApplicationContext(),"password is too short",Toast.LENGTH_LONG).show();
                 return;
             }
             String name=inputName.getText().toString().trim();
             String phoneno=inputPhoneno.getText().toString().trim();
             String confirmpass=inputconfirmPass.getText().toString().trim();
             Paper.book().write(MyRecs.USER_EMAIL,inputEmail.getText().toString());
             Paper.book().write(MyRecs.USER_NAME,inputName.getText().toString());
             Paper.book().write(MyRecs.USER_PHONE,inputPhoneno.getText().toString());
             progressBar.setVisibility(View.VISIBLE);
//i stopped here
             Task<AuthResult> authResultTask = auth.createUserWithEmailAndPassword(email, password)
                     .addOnCompleteListener(RegisterActivity.this, new onCompleteListener<AuthResult>() {
                         private Task<AuthResult> task;

                         @Override
                         public void onComplete(@NonNull Task<AuthResult> task) {
                             this.task = task;

                             Toast.makeText(RegisterActivity.this, "Registration to MulikaMwizi is successful!" , Toast.LENGTH_LONG).show();
                             progressBar.setVisibility(View.GONE);
                             if (!task.isSuccessful()) {
                                 Toast.makeText(RegisterActivity.this, "Failed to register Account,please check your internet connection." , Toast.LENGTH_LONG).show();
                             } else {

                                 startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                 finish();
                             }
                         }

                     });
         }
     });
        TextView loginScreen = findViewById(R.id.link_to_login);

        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener(){

            public void onClick(View arg0) {
                // Closing registration screen
                // Switching to Login Screen/closing register screen
                finish();
            }
        });

    }

}
