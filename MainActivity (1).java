package com.example.medicalconsultation;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.BookAppointmentPages.BookAppointmentSpecActivity;
import com.example.medicalconsultation.LoginPages.LoginActivity;
import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.SearchMedicinePages.SearchMedicineMapActivity;
import com.example.medicalconsultation.SymptomCheckerPages.SymptomCheckerSplashScreen;
import com.example.medicalconsultation.SymptomCheckerPages.SymptomCheckerUserDataActivity;
import com.example.medicalconsultation.YourAppointmentsPages.YourAppointmentsDocSelectActivity;
import com.example.medicalconsultation.YourPrescriptionPages.YourPrescriptionSelectDocActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    Button logoutButton,bookAppointmentButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    TextView userFullNameText,userGenderText,userAgeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.home_action_bar);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        userFullNameText=(TextView)findViewById(R.id.UserFullNameText);
        userGenderText=(TextView)findViewById(R.id.UserGenderText);
        userAgeText=(TextView)findViewById(R.id.UserAgeText);

        bookAppointmentButton=(Button)findViewById(R.id.BookAppointmentButton);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        setUserInfo();

        bookAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BookAppointmentSpecActivity.class));
            }
        });

    }

    private void setUserInfo() {
        if(fAuth.getCurrentUser()!=null) {
            String uid = fAuth.getCurrentUser().getUid();
            DocumentReference userInfoDocumentReference = fStore.collection("Users").document(uid);
            userInfoDocumentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null)
                        e.printStackTrace();
                    else{
                        userFullNameText.setText(documentSnapshot.getString("fullName"));
                        userGenderText.setText(documentSnapshot.getString("gender"));
                        userAgeText.setText(documentSnapshot.getString("age") + " yrs");
                    }
                }
            });
        }
    }

    public void logout(View view) {
        if (fAuth.getCurrentUser() != null) {
            fAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginAsActivity.class));
            finish();
        }
    }

    public void yourAppointmentsButton(View view){
        startActivity(new Intent(MainActivity.this, YourAppointmentsDocSelectActivity.class));
    }

    public void SearchMedicine(View view){
        startActivity(new Intent(MainActivity.this, SearchMedicineMapActivity.class));
    }

    public void YourPrescriptionButton(View view){
        startActivity(new Intent(MainActivity.this, YourPrescriptionSelectDocActivity.class));
    }

    public void checkSymptomButton(View view){
        //startActivity(new Intent(MainActivity.this, SymptomCheckerSplashScreen.class));
        Toast.makeText(MainActivity.this, "Feature Coming Soon!", Toast.LENGTH_SHORT).show();
    }

}
