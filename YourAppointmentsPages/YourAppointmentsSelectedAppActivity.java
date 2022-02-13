package com.example.medicalconsultation.YourAppointmentsPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.medicalconsultation.BookAppointmentPages.BookAppointmentDocSelectActivity;
import com.example.medicalconsultation.BookAppointmentPages.BookAppointmentSelectedDocActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.YourPrescriptionPages.YourPrescriptionSelectedPresActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class YourAppointmentsSelectedAppActivity extends AppCompatActivity {

    TextView docNameText,docSpecText,docGenderText,docExpText,docCurrentPracticeText,docDegreeText,timeSlotText,selectedDateText;
    String docUID,date,timeSlot;
    Button backButton;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_appointments_selected_app);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        docNameText=(TextView)findViewById(R.id.DocNameText);
        docSpecText=(TextView)findViewById(R.id.DocSpecText);
        docGenderText=(TextView)findViewById(R.id.DocGenderText);
        docExpText=(TextView)findViewById(R.id.DocExpText);
        docCurrentPracticeText=(TextView)findViewById(R.id.DocCurrentPracticeText);
        docDegreeText=(TextView)findViewById(R.id.DocDegreeText);
        timeSlotText=(TextView)findViewById(R.id.TimeSlotText);
        selectedDateText=(TextView)findViewById(R.id.SelectedDateText);
        backButton=(Button)findViewById(R.id.BackButton);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        Bundle bundle=new Bundle();
        bundle=getIntent().getExtras();
        docUID=bundle.getString("docUID");
        date=bundle.getString("date");
        timeSlot=bundle.getString("timeSlot");

        timeSlotText.setText(timeSlot);
        selectedDateText.setText(date);

        setDocInfo();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(YourAppointmentsSelectedAppActivity.this, YourAppointmentsDocSelectActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void meetLinkButton(View view){

        if(fAuth.getCurrentUser()!=null){
            fStore.collection("Users").document(fAuth.getUid()).collection("Appointments").document(docUID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    String meetLink;
                    if(e!=null)
                        e.printStackTrace();
                    else{
                        meetLink=documentSnapshot.getString("meetLink");
                        try{
                            Intent openGmeet = new Intent("android.intent.action.VIEW", Uri.parse(meetLink));
                            startActivity(openGmeet);
                        }catch(Exception el){
                            el.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void yourPrescriptionButton(View view){
        Intent intent=new Intent(YourAppointmentsSelectedAppActivity.this, YourPrescriptionSelectedPresActivity.class);
        intent.putExtra("docUID",docUID);
        intent.putExtra("date",date);
        intent.putExtra("timeSlot",timeSlot);
        startActivity(intent);
        finish();
    }

    private void setDocInfo() {
        if(fAuth.getCurrentUser()!=null){
            final DocumentReference documentReference=fStore.collection("Doctors").document(docUID);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null){
                        e.printStackTrace();
                    }else{
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
}
