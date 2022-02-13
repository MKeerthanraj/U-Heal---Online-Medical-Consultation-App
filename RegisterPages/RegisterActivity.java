package com.example.medicalconsultation.RegisterPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.LoginPages.LoginActivity;
import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText fullNameEdit,emailEdit,passwordEdit,phoneEdit,ageEdit;
    RadioButton maleRadio,femaleRadio,otherRadio;
    Button loginButton;
    Button registerButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.login_as_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        fullNameEdit=(EditText)findViewById(R.id.FullNameEdit);
        emailEdit=(EditText)findViewById(R.id.EmailEdit);
        passwordEdit=(EditText)findViewById(R.id.PasswordEdit);
        phoneEdit=(EditText)findViewById(R.id.PhoneEdit);
        loginButton=(Button) findViewById(R.id.LoginButton);
        registerButton=(Button)findViewById(R.id.RegisterButton);
        progressBar=(ProgressBar)findViewById(R.id.Progress);
        maleRadio=(RadioButton)findViewById(R.id.MaleRadio);
        femaleRadio=(RadioButton)findViewById(R.id.FemaleRadio);
        otherRadio=(RadioButton)findViewById(R.id.OtherRadio);
        ageEdit=(EditText)findViewById(R.id.AgeEdit);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterActivity.this, LoginAsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName=fullNameEdit.getText().toString();
                final String email=emailEdit.getText().toString().trim();
                String password=passwordEdit.getText().toString().trim();
                final String phone=phoneEdit.getText().toString();
                final String gender=getGender();
                final String age=ageEdit.getText().toString();

                if(fullName.isEmpty()){
                    fullNameEdit.setError("Required");
                    return;
                }
                if(email.isEmpty()){
                    emailEdit.setError("Email Required");
                    return;
                }
                if(password.isEmpty()){
                    passwordEdit.setError("Password Required");
                    return;
                }
                if(password.length()<6){
                    passwordEdit.setError("Must be greater than 6 character");
                    return;
                }
                if(phone.isEmpty()){
                    phoneEdit.setError("Required");
                    return;
                }
                if(gender.matches("NA")){
                    Toast.makeText(RegisterActivity.this, "Select Gender", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(age.isEmpty()){
                    ageEdit.setError("Required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                disableInputs();

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> user = new HashMap<>();
                            user.put("fullName",fullName);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put("gender",gender);
                            user.put("age",age);

                            fStore.collection("Users").document(fAuth.getUid()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(RegisterActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(RegisterActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    enableInputs();
                                    Toast.makeText(RegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });

                        }
                        else{
                            enableInputs();
                            Exception e=task.getException();
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

    }

    private void disableInputs() {
        passwordEdit.setEnabled(false);
        fullNameEdit.setEnabled(false);
        emailEdit.setEnabled(false);
        ageEdit.setEnabled(false);
        phoneEdit.setEnabled(false);
        maleRadio.setEnabled(false);
        femaleRadio.setEnabled(false);
        otherRadio.setEnabled(false);
        registerButton.setEnabled(false);
        loginButton.setEnabled(false);
    }

    private void enableInputs() {
        passwordEdit.setEnabled(true);
        fullNameEdit.setEnabled(true);
        emailEdit.setEnabled(true);
        ageEdit.setEnabled(true);
        phoneEdit.setEnabled(true);
        maleRadio.setEnabled(true);
        femaleRadio.setEnabled(true);
        otherRadio.setEnabled(true);
        registerButton.setEnabled(true);
        loginButton.setEnabled(true);
    }

    private String getGender() {
        if(maleRadio.isChecked())
            return "Male";
        else if(femaleRadio.isChecked())
            return "Female";
        else if(otherRadio.isChecked())
            return "Other";
        else
            return "NA";
    }
}
