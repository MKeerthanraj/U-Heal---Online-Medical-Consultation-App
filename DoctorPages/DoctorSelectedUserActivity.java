package com.example.medicalconsultation.DoctorPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.medicalconsultation.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class DoctorSelectedUserActivity extends AppCompatActivity {

    Button backButton,linkConformationButton,openLinkButton;
    EditText linkEdit;
    TextView userNameText,userGenderText,userAgeText,timeSlotText,selectedDateText;
    String userUID,date,timeSlot,meetLink;
    Drawable ic_check,ic_delete;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_selected_user);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        backButton=(Button)findViewById(R.id.BackButton);
        openLinkButton=(Button)findViewById(R.id.OpenLinkButton);
        linkConformationButton=(Button)findViewById(R.id.LinkConformationButton);
        linkEdit=(EditText)findViewById(R.id.LinkEdit);
        timeSlotText=(TextView)findViewById(R.id.TimeSlotText);
        selectedDateText=(TextView)findViewById(R.id.SelectedDateText);
        userNameText=(TextView)findViewById(R.id.UserNameText);
        userGenderText=(TextView)findViewById(R.id.UserGenderText);
        userAgeText=(TextView)findViewById(R.id.UserAgeText);

        ic_check=getResources().getDrawable(R.drawable.ic_check_green);
        ic_delete=getResources().getDrawable(R.drawable.ic_delete_red);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        Bundle bundle=new Bundle();
        bundle=getIntent().getExtras();
        userUID=bundle.getString("userUID");
        date=bundle.getString("date");
        timeSlot=bundle.getString("timeSlot");

        timeSlotText.setText(timeSlot);
        selectedDateText.setText(date);

        setUserInfo();
        checkMeetLinkAlreadyProvided();

        linkConformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(linkConformationButton.getTag().toString().matches("check")){
                    linkConformationButton.setTag("delete");
                    meetLink=linkEdit.getText().toString();
                    if(!(meetLink.startsWith("https://"))){
                        meetLink="https://"+meetLink;
                    }
                    linkEdit.setEnabled(false);
                    updateMeetLinkInFStore();
                    openLinkButton.setEnabled(true);
                    linkConformationButton.setCompoundDrawablesWithIntrinsicBounds(null,ic_delete,null,null);
                }
                else if(linkConformationButton.getTag().toString().matches("delete")){
                    linkConformationButton.setTag("check");
                    meetLink="";
                    openLinkButton.setEnabled(false);
                    updateMeetLinkInFStore();
                    linkEdit.setEnabled(true);
                    linkEdit.setText(null);
                    linkEdit.setHint("Paste Meet Link here");
                    linkConformationButton.setCompoundDrawablesWithIntrinsicBounds(null, ic_check , null, null);
                }
            }
        });

        backButton=(Button)findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void checkMeetLinkAlreadyProvided() {
        if(fAuth.getCurrentUser()!=null){
            fStore.collection("Doctors").document(fAuth.getUid()).collection("Appointments").document(userUID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null)
                        e.printStackTrace();
                    else{
                        if(documentSnapshot.getString("meetLink")!=null){
                            if(!(documentSnapshot.getString("meetLink").matches(""))){
                                linkEdit.setText(documentSnapshot.getString("meetLink"));
                                linkEdit.setEnabled(false);
                                openLinkButton.setEnabled(true);
                                linkConformationButton.setCompoundDrawablesWithIntrinsicBounds(null,ic_delete,null,null);
                            }
                        }
                    }
                }
            });
        }
    }

    private void updateMeetLinkInFStore() {
        if(fAuth.getCurrentUser()!=null){
            fStore.collection("Doctors").document(fAuth.getUid()).collection("Appointments").document(userUID).update("meetLink",meetLink);
            fStore.collection("Users").document(userUID).collection("Appointments").document(fAuth.getUid()).update("meetLink",meetLink);
        }
    }

    public void meetLinkButton(View view){
        if(fAuth.getCurrentUser()!=null){
            fStore.collection("Doctors").document(fAuth.getUid()).collection("Appointments").document(userUID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                        }catch (Exception el) {
                            el.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public void uploadPrescriptionButton(View view){
        Intent intent=new Intent(DoctorSelectedUserActivity.this,DoctorUploadPrescriptionActivity.class);
        intent.putExtra("userUID",userUID);
        intent.putExtra("date",date);
        intent.putExtra("timeSlot",timeSlot);
        intent.putExtra("meetLink",meetLink);
        startActivity(intent);
    }

    private void setUserInfo() {
        if(fAuth.getCurrentUser()!=null){
            final DocumentReference documentReference=fStore.collection("Users").document(userUID);
            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null){
                        e.printStackTrace();
                    }else{
                        userNameText.setText(documentSnapshot.get("fullName").toString());
                        userGenderText.setText(documentSnapshot.get("gender").toString());
                        userAgeText.setText(documentSnapshot.get("age").toString()+" yrs");
                    }
                }
            });
        }
    }


}
