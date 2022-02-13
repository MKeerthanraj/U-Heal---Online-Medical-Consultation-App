package com.example.medicalconsultation.LoginPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.medicalconsultation.ChemistPages.ChemistMainActivity;
import com.example.medicalconsultation.DoctorPages.DoctorMainAcitivty;
import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.RegisterPages.RegisterAsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginAsActivity extends AppCompatActivity {

    Button userLoginButton,doctorLoginButton,chemistLoginButton,registerButton;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_as);
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.login_as_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        userLoginButton=(Button)findViewById(R.id.UserLoginButton);
        doctorLoginButton=(Button)findViewById(R.id.DoctorLoginButton);
        chemistLoginButton=(Button)findViewById(R.id.ChemistLoginButton);
        registerButton=(Button)findViewById(R.id.RegisterButton);
        progressBar=(ProgressBar)findViewById(R.id.Progress);

        fAuth=FirebaseAuth.getInstance();
        fStore= FirebaseFirestore.getInstance();

        userLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fAuth.getCurrentUser()!=null){
                    progressBar.setVisibility(View.VISIBLE);
                    fStore.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int counter=0;
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot DOCUMENT : task.getResult()) {
                                    if(DOCUMENT.getId().matches(fAuth.getUid())){
                                        counter++;
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(LoginAsActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                                if(counter==0){
                                    fAuth.signOut();
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(new Intent(LoginAsActivity.this, LoginActivity.class));
                                    finish();
                                }
                            }
                        }
                    });
                }
                else
                    startActivity(new Intent(LoginAsActivity.this,LoginActivity.class));
            }
        });

        doctorLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fAuth.getCurrentUser()!=null){
                    progressBar.setVisibility(View.VISIBLE);
                    fStore.collection("Doctors").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int counter=0;
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot DOCUMENT : task.getResult()) {
                                    if(DOCUMENT.getId().matches(fAuth.getUid())){
                                        counter++;
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(LoginAsActivity.this, DoctorMainAcitivty.class));
                                        finish();
                                    }
                                }
                                if(counter==0){
                                    fAuth.signOut();
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(new Intent(LoginAsActivity.this,DoctorLoginActivity.class));
                                }
                            }
                        }
                    });
                }
                else
                    startActivity(new Intent(LoginAsActivity.this,DoctorLoginActivity.class));
            }
        });

        chemistLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fAuth.getCurrentUser()!=null){
                    progressBar.setVisibility(View.VISIBLE);
                    fStore.collection("Pharmacy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            int counter=0;
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot DOCUMENT : task.getResult()) {
                                    if(DOCUMENT.getId().matches(fAuth.getUid())){
                                        counter++;
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(LoginAsActivity.this, ChemistMainActivity.class));
                                        finish();
                                    }
                                }
                                if(counter==0){
                                    fAuth.signOut();
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(new Intent(LoginAsActivity.this,ChemistLoginActivity.class));
                                }
                            }
                        }
                    });
                }
                else
                    startActivity(new Intent(LoginAsActivity.this,ChemistLoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginAsActivity.this, RegisterAsActivity.class));
            }
        });
    }
}
