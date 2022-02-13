package com.example.medicalconsultation.DoctorPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class DoctorMainAcitivty extends AppCompatActivity {

    Button logoutButton,yourAppointmentsButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    TextView docNameText,docSpecText,docGenderText,docExpText,docCurrentPracticeText,docDegreeText,docAgeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_main_acitivty);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.home_action_bar);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        docNameText=(TextView)findViewById(R.id.DocNameText);
        docSpecText=(TextView)findViewById(R.id.DocSpecText);
        docGenderText=(TextView)findViewById(R.id.DocGenderText);
        docAgeText=(TextView)findViewById(R.id.DocAgeText);
        docExpText=(TextView)findViewById(R.id.DocExpText);
        docCurrentPracticeText=(TextView)findViewById(R.id.DocCurrentPracticeText);
        docDegreeText=(TextView)findViewById(R.id.DocDegreeText);

        yourAppointmentsButton=(Button)findViewById(R.id.YourAppointmentsButton);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        setDocInfo();

        yourAppointmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DoctorMainAcitivty.this,DoctorAppointmentSelectActivity.class));
            }
        });
    }

    private void setDocInfo() {
        if(fAuth.getCurrentUser()!=null){
            final DocumentReference documentReference=fStore.collection("Doctors").document(fAuth.getUid());
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null){
                        e.printStackTrace();
                    }else{
                        docAgeText.setText(documentSnapshot.get("age").toString()+" yrs");
                        docNameText.setText(documentSnapshot.get("fullName").toString());
                        docSpecText.setText(documentSnapshot.get("specialization").toString());
                        docExpText.setText("Experience : "+documentSnapshot.get("experience").toString()+" yrs");
                        docGenderText.setText(documentSnapshot.get("gender").toString());
                        docCurrentPracticeText.setText("Currently Practicing at :\n"+documentSnapshot.get("presentWork").toString());
                        docDegreeText.setText("Degree : "+documentSnapshot.get("degree").toString());
                    }
                }
            });
        }
    }

    public void logout(View view) {
        if (fAuth.getCurrentUser() != null) {
            fAuth.signOut();
            startActivity(new Intent(DoctorMainAcitivty.this, LoginAsActivity.class));
            finish();
        }
    }
}
