package com.example.medicalconsultation.BookAppointmentPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class BookAppointmentDocSelectActivity extends AppCompatActivity {

    Button backButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String selectedSpec;
    LinearLayout doctorListLinearLayout;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment_doc_select);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        doctorListLinearLayout=(LinearLayout)findViewById(R.id.DoctorListLinearLayout);
        progressBar=(ProgressBar)findViewById(R.id.Progress);

        Bundle bundle=new Bundle();
        bundle=getIntent().getExtras();
        selectedSpec=bundle.getString("Spec");

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        if(fAuth.getCurrentUser()!=null){
            fStore.collection("SearchDoctor")
                    .document(selectedSpec)
                    .collection("docs")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> TASK) {
                            if (TASK.isSuccessful()) {
                                for (QueryDocumentSnapshot DOCUMENT : TASK.getResult()) {
                                    addDocCard(DOCUMENT.getData().get("docUID").toString());
                                }
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Log.i(selectedSpec, "Error getting documents: ", TASK.getException());
                            }
                        }
                    });

        }

        backButton=(Button)findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(BookAppointmentDocSelectActivity.this,BookAppointmentSpecActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addDocCard(final String docId) {
        View docCard= getLayoutInflater().inflate(R.layout.doc_prescription_card,null,false);
        View spaceBetween=getLayoutInflater().inflate(R.layout.space_between_cards,null,false);

        docCard.setBackgroundResource(R.drawable.card_bg_green);

        final TextView doctorNameText=(TextView)docCard.findViewById(R.id.DoctorNameText);
        final TextView doctorSpecText=(TextView)docCard.findViewById(R.id.DoctorSpecText);
        final TextView otherInfoText=(TextView)docCard.findViewById(R.id.OtherInfoText);

        if(fAuth.getCurrentUser()!=null) {
            fStore.collection("Doctors").document(docId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (e != null)
                        e.printStackTrace();
                    else {
                        doctorNameText.setText(documentSnapshot.getString("fullName"));
                        doctorSpecText.setText(documentSnapshot.getString("specialization"));
                        otherInfoText.setText("Experience : "+documentSnapshot.getString("experience") + " yrs");
                    }
                }
            });
        }

        docCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(BookAppointmentDocSelectActivity.this,BookAppointmentSelectedDocActivity.class);
                intent.putExtra("docUID",docId);
                intent.putExtra("Spec",selectedSpec);
                startActivity(intent);
                finish();
            }
        });

        doctorListLinearLayout.addView(docCard);
        doctorListLinearLayout.addView(spaceBetween);
    }

}
