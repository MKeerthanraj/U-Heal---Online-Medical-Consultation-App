package com.example.medicalconsultation.BookAppointmentPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class BookAppointmentSelectedDocActivity extends AppCompatActivity {

    TextView docNameText,docSpecText,docGenderText,docExpText,docCurrentPracticeText,docDegreeText;
    Spinner timeSlotSpinner;
    String docUID;
    int year,month,day;
    String selectedSpec,selectedTimeSlot,selectedDate;
    Button backButton,datePickerButton,bookButton;
    Calendar calendar;
    ProgressBar progressBar;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    ArrayList<String> timeSlotsList;
    ArrayAdapter timeSlotSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment_selected_doc);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        timeSlotsList=new ArrayList<String>();

        timeSlotsList.add("10am-11am");
        timeSlotsList.add("11am-12pm");
        timeSlotsList.add("12pm-1pm");
        timeSlotsList.add("1pm-2pm");
        timeSlotsList.add("2pm-3pm");
        timeSlotsList.add("3pm-4pm");
        timeSlotsList.add("4pm-5pm");
        timeSlotsList.add("5pm-6pm");

        docNameText=(TextView)findViewById(R.id.DocNameText);
        docSpecText=(TextView)findViewById(R.id.DocSpecText);
        docGenderText=(TextView)findViewById(R.id.DocGenderText);
        docExpText=(TextView)findViewById(R.id.DocExpText);
        docCurrentPracticeText=(TextView)findViewById(R.id.DocCurrentPracticeText);
        docDegreeText=(TextView)findViewById(R.id.DocDegreeText);
        timeSlotSpinner=(Spinner)findViewById(R.id.TimeSlotSpinner);
        backButton=(Button)findViewById(R.id.BackButton);
        datePickerButton=(Button)findViewById(R.id.DatePickerButton);
        progressBar=(ProgressBar)findViewById(R.id.Progress);
        bookButton=(Button)findViewById(R.id.BookAppointmentButton);

        calendar=Calendar.getInstance();

        timeSlotSpinnerAdapter=new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,timeSlotsList);
        timeSlotSpinner.setAdapter(timeSlotSpinnerAdapter);
        timeSlotSpinner.setSelection(0);

        timeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeSlot=timeSlotsList.get(position);
                timeSlotSpinner.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year=calendar.get(Calendar.YEAR);
                month=calendar.get(Calendar.MONTH);
                day=calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog=new DatePickerDialog(BookAppointmentSelectedDocActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, month, dayOfMonth);
                        datePickerButton.setText(SimpleDateFormat.getDateInstance().format(newDate.getTime()));
                        selectedDate=SimpleDateFormat.getDateInstance().format(newDate.getTime());
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        Bundle bundle=new Bundle();
        bundle=getIntent().getExtras();
        docUID=bundle.getString("docUID");
        selectedSpec=bundle.getString("Spec");

        setDocInfo();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(BookAppointmentSelectedDocActivity.this,BookAppointmentDocSelectActivity.class);
                intent.putExtra("Spec",selectedSpec);
                startActivity(intent);
                finish();
            }
        });

    }

    public void bookAppointmentButton(View view){
        if(selectedDate==null){
            Toast.makeText(this, "Select Date", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(selectedTimeSlot==null){
            Toast.makeText(this, "Select Time-Slot", Toast.LENGTH_SHORT).show();
            return;
        }
        if(fAuth.getCurrentUser()!=null){
            Map<String,Object> docsAppointmentRecord=new HashMap<String, Object>();
            docsAppointmentRecord.put("userUID",fAuth.getUid());
            docsAppointmentRecord.put("date",selectedDate);
            docsAppointmentRecord.put("timeSlot",selectedTimeSlot);
            docsAppointmentRecord.put("meetLink","");

            Map<String,Object> usersAppointmentRecord=new HashMap<String, Object>();
            usersAppointmentRecord.put("docUID",docUID);
            usersAppointmentRecord.put("date",selectedDate);
            usersAppointmentRecord.put("timeSlot",selectedTimeSlot);
            usersAppointmentRecord.put("meetLink","");

            progressBar.setVisibility(View.VISIBLE);
            bookButton.setEnabled(false);

            fStore.collection("Users").document(fAuth.getUid()).collection("Appointments").document(docUID).set(usersAppointmentRecord).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(BookAppointmentSelectedDocActivity.this, "Appointment Booked Successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    bookButton.setEnabled(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BookAppointmentSelectedDocActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    bookButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            });

            fStore.collection("Doctors").document(docUID).collection("Appointments").document(fAuth.getUid()).set(docsAppointmentRecord).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    startActivity(new Intent(BookAppointmentSelectedDocActivity.this, MainActivity.class));
                    finish();
                    progressBar.setVisibility(View.GONE);
                    bookButton.setEnabled(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    bookButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            });


        }
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
