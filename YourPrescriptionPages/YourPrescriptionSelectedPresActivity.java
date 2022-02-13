package com.example.medicalconsultation.YourPrescriptionPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.DoctorPages.DoctorUploadPrescriptionActivity;
import com.example.medicalconsultation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

public class YourPrescriptionSelectedPresActivity extends AppCompatActivity {

    Button backButton;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ImageView prescriptionImageView;
    StorageReference storageReference;
    String docUID,date,timeSlot,meetLink;
    ProgressBar progressBar;
    TextView prescriptionDetailsText,prescriptonDocNameText,progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_prescription_selected_pres);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        backButton=(Button)findViewById(R.id.BackButton);

        progressBar=(ProgressBar)findViewById(R.id.Progress);
        prescriptionDetailsText=(TextView)findViewById(R.id.PrescriptionDetailsText);
        prescriptonDocNameText=(TextView)findViewById(R.id.PrescriptionDocNameText);
        progressText=(TextView)findViewById(R.id.ProgressText);

        Bundle bundle=new Bundle();
        bundle=getIntent().getExtras();
        docUID=bundle.getString("docUID");
        date=bundle.getString("date");
        timeSlot=bundle.getString("timeSlot");
        setDocInfo();

        prescriptionImageView=(ImageView)findViewById(R.id.UserPrescriptionImageView);

        setExistingImageFromCloud();
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setExistingImageFromCloud() {
        if(fAuth.getCurrentUser()!=null){
            StorageReference prescriptionRef=storageReference.child(docUID+"/"+fAuth.getUid()+"/"+date+"_"+timeSlot);
            try {
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                backButton.setEnabled(false);
                final File tempFile=File.createTempFile("temp","jpg");
                prescriptionRef.getFile(tempFile)
                        .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressBar.setProgress((int)progress);
                                progressText.setText("Downloading Prescription : "+(int)progress+"%");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                progressBar.setVisibility(View.GONE);
                                progressText.setVisibility(View.GONE);
                                backButton.setEnabled(true);
                                Bitmap bitmap= BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                                bitmap=adjustOrientation(bitmap,tempFile.getAbsolutePath());
                                prescriptionImageView.setImageBitmap(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(YourPrescriptionSelectedPresActivity.this, "Prescriptions not uploaded yet", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);
                        backButton.setEnabled(true);
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                        String docName=documentSnapshot.get("fullName").toString();
                        String docSpec=documentSnapshot.get("specialization").toString();
                        prescriptonDocNameText.setText(docName);
                        prescriptionDetailsText.setText(docSpec+"\n"+date+"   "+timeSlot);
                    }
                }
            });
        }
    }

    private Bitmap adjustOrientation(Bitmap bitmap, String path) {
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            return rotatedBitmap;
        }catch (Exception e){
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}
