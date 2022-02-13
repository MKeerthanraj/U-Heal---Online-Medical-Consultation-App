package com.example.medicalconsultation.LoginPages;

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
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.ChemistPages.ChemistMainActivity;
import com.example.medicalconsultation.DoctorPages.DoctorMainAcitivty;
import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.RegisterPages.RegisterActivity;
import com.example.medicalconsultation.RegisterPages.RegisterAsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ChemistLoginActivity extends AppCompatActivity {

    EditText emailEdit,passwordEdit;
    Button registerButton;
    Button loginButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemist_login);
        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.login_as_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        emailEdit=(EditText)findViewById(R.id.EmailEdit);
        passwordEdit=(EditText)findViewById(R.id.PasswordEdit);
        registerButton=(Button)findViewById(R.id.RegisterButton);
        loginButton=(Button)findViewById(R.id.LoginButton);
        progressBar=(ProgressBar)findViewById(R.id.Progress);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChemistLoginActivity.this, RegisterAsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if(fAuth.getCurrentUser()!=null){
            startActivity(new Intent(ChemistLoginActivity.this, ChemistMainActivity.class));
            finish();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailEdit.getText().toString().trim();
                String password=passwordEdit.getText().toString().trim();

                if(email.isEmpty()){
                    emailEdit.setError("Email Required");
                    return;
                }
                if(password.isEmpty()){
                    passwordEdit.setError("Password Required");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            if(fAuth.getCurrentUser()!=null){
                                fStore.collection("Pharmacy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        int counter=0;
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot DOCUMENT : task.getResult()) {
                                                if(DOCUMENT.getId().matches(fAuth.getUid())){
                                                    counter++;
                                                    Toast.makeText(ChemistLoginActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ChemistLoginActivity.this, ChemistMainActivity.class));
                                                    finish();
                                                }
                                            }
                                            if(counter==0){
                                                fAuth.signOut();
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(ChemistLoginActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            Toast.makeText(ChemistLoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }
}
