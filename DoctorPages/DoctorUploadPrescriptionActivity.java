package com.example.medicalconsultation.DoctorPages;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DoctorUploadPrescriptionActivity extends AppCompatActivity {

    public static final int CAMERA_PERM_CODE = 8055;
    public static final int CAMERA_REQUEST_CODE = 999;
    Button backButton,clickButton;
    ImageView prescriptionImageView;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    StorageReference storageReference;
    String currentPhotoPath;
    String userUID,date,timeSlot,meetLink;
    ProgressBar progressBar;
    TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_upload_prescription);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();

        progressBar=(ProgressBar)findViewById(R.id.Progress);
        progressText=(TextView)findViewById(R.id.ProgressText);

        Bundle bundle=new Bundle();
        bundle=getIntent().getExtras();
        userUID=bundle.getString("userUID");
        date=bundle.getString("date");
        timeSlot=bundle.getString("timeSlot");
        meetLink=bundle.getString("meetLink");

        backButton=(Button)findViewById(R.id.BackButton);
        clickButton=(Button)findViewById(R.id.ClickPhotoButton);
        prescriptionImageView=(ImageView)findViewById(R.id.PrescriptionImageView);
        setExistingImageFromCloud();

        backButton=(Button)findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setExistingImageFromCloud() {
        if(fAuth.getCurrentUser()!=null){
            StorageReference prescriptionRef=storageReference.child(fAuth.getUid()+"/"+userUID+"/"+date+"_"+timeSlot);
            try {
                progressBar.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);
                backButton.setEnabled(false);
                clickButton.setEnabled(false);
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
                                clickButton.setEnabled(true);
                                Bitmap bitmap=BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                                bitmap=adjustOrientation(bitmap,tempFile.getAbsolutePath());
                                prescriptionImageView.setImageBitmap(bitmap);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DoctorUploadPrescriptionActivity.this, "Prescriptions not uploaded yet", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);
                        backButton.setEnabled(true);
                        clickButton.setEnabled(true);
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void clickPhotoButton(View view){
        askCameraPermission();
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA},CAMERA_PERM_CODE);
        }else{
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==CAMERA_PERM_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                openCamera();
            }
            else {
                Toast.makeText(this, "Permissions Needed!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        try {
            File imageFile=createImageFile();
            Uri imageUri=FileProvider.getUriForFile(DoctorUploadPrescriptionActivity.this,"com.example.medicalconsultation.fileprovider",imageFile);

            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

            startActivityForResult(intent,CAMERA_REQUEST_CODE);
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    private File createImageFile() throws IOException {
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "photo";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CAMERA_REQUEST_CODE&&resultCode==RESULT_OK){
            Bitmap bitmap=BitmapFactory.decodeFile(currentPhotoPath);
            bitmap=adjustOrientation(bitmap,currentPhotoPath);
            prescriptionImageView.setImageBitmap(bitmap);
            uploadImageToCloud();
        }
    }

    private void uploadImageToCloud() {
        if (fAuth.getCurrentUser() != null) {
            progressBar.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Uploading Image please wait...", Toast.LENGTH_LONG).show();
            backButton.setEnabled(false);
            clickButton.setEnabled(false);
            Uri imageUri=Uri.fromFile(new File(currentPhotoPath));
            StorageReference presentDocFolder=storageReference.child(fAuth.getUid()+"/"+userUID+"/"+date+"_"+timeSlot);

            presentDocFolder.putFile(imageUri)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressBar.setProgress((int)progress);
                            progressText.setText("Uploading Image : "+(int)progress+"%");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(DoctorUploadPrescriptionActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            backButton.setEnabled(true);
                            clickButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Toast.makeText(DoctorUploadPrescriptionActivity.this, "Error Uploading Image", Toast.LENGTH_SHORT).show();
                    backButton.setEnabled(true);
                    clickButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                    prescriptionImageView.setImageResource(R.drawable.ic_upload_pres_bg);
                }
            });
        }
    }

    private Bitmap adjustOrientation(Bitmap bitmap,String path) {
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
