package com.example.medicalconsultation.DoctorPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.medicalconsultation.BookAppointmentPages.BookAppointmentDocSelectActivity;
import com.example.medicalconsultation.BookAppointmentPages.BookAppointmentSpecActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.YourAppointmentsPages.YourAppointmentsDocSelectActivity;
import com.example.medicalconsultation.YourAppointmentsPages.YourAppointmentsSelectedAppActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class DoctorAppointmentSelectActivity extends AppCompatActivity {

    Button backButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    LinearLayout appointmentListLinearLayout;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_appointment_select);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        appointmentListLinearLayout=(LinearLayout)findViewById(R.id.DoctorAppointmentListLinearLayout);
        progressBar=(ProgressBar)findViewById(R.id.Progress);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser()!=null){
            fStore.collection("Doctors")
                    .document(fAuth.getUid())
                    .collection("Appointments")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> TASK) {
                            if (TASK.isSuccessful()) {
                                String userUID,date,timeSlot;
                                progressBar.setVisibility(View.VISIBLE);
                                for (QueryDocumentSnapshot DOCUMENT : TASK.getResult()) {
                                    userUID=DOCUMENT.getData().get("userUID").toString();
                                    date=DOCUMENT.getData().get("date").toString();
                                    timeSlot=DOCUMENT.getData().get("timeSlot").toString();
                                    addAppointmentCard(userUID, date, timeSlot);
                                }
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Log.i("error", "Error getting documents: ", TASK.getException());
                            }
                        }
                    });
        }

        backButton=(Button)findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addAppointmentCard(final String userUID, final String date, final String timeSlot) {
        View appointmentCard= getLayoutInflater().inflate(R.layout.doc_prescription_card,null,false);
        View spaceBetween=getLayoutInflater().inflate(R.layout.space_between_cards,null,false);

        appointmentCard.setBackgroundResource(R.drawable.card_bg_green);

        final TextView doctorNameText=(TextView)appointmentCard.findViewById(R.id.DoctorNameText);
        final TextView doctorSpecText=(TextView)appointmentCard.findViewById(R.id.DoctorSpecText);
        final TextView otherInfoText=(TextView)appointmentCard.findViewById(R.id.OtherInfoText);

        otherInfoText.setText(date+"/"+timeSlot);

        if(fAuth.getCurrentUser()!=null) {
            fStore.collection("Users").document(userUID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null)
                        e.printStackTrace();
                    else {
                        doctorNameText.setText(documentSnapshot.getString("fullName"));
                        doctorSpecText.setText(documentSnapshot.getString("gender")+" "+documentSnapshot.getString("age")+" yrs");
                    }
                }
            });
        }

        appointmentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DoctorAppointmentSelectActivity.this, DoctorSelectedUserActivity.class);
                intent.putExtra("userUID",userUID);
                intent.putExtra("date",date);
                intent.putExtra("timeSlot",timeSlot);
                startActivity(intent);
            }
        });

        appointmentListLinearLayout.addView(appointmentCard);
        appointmentListLinearLayout.addView(spaceBetween);

    }
}
