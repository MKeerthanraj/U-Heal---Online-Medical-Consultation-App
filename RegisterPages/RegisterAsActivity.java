package com.example.medicalconsultation.RegisterPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.R;

public class RegisterAsActivity extends AppCompatActivity {

    Button userRegisterButton,doctorRegisterButton,chemistRegisterButton,loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_as);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.login_as_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        userRegisterButton=(Button)findViewById(R.id.UserRegisterButton);
        doctorRegisterButton=(Button)findViewById(R.id.DoctorRegisterButton);
        chemistRegisterButton=(Button)findViewById(R.id.ChemistRegisterButton);
        loginButton=(Button)findViewById(R.id.LoginButton);

        userRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterAsActivity.this, RegisterActivity.class));
            }
        });

        doctorRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterAsActivity.this, DoctorRegisterActivity.class));
            }
        });

        chemistRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterAsActivity.this, ChemistRegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterAsActivity.this, LoginAsActivity.class));
            }
        });

    }
}
