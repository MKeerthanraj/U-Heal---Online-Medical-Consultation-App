package com.example.medicalconsultation.RegisterPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.ChemistPages.ChemistMainActivity;
import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.SearchMedicinePages.SearchMedicineMapActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChemistRegisterActivity extends AppCompatActivity implements OnMapReadyCallback {

    ProgressBar progressBar;
    EditText pharmaNameEdit,emailEdit,passwordEdit,phoneEdit;
    Button loginButton;
    Button registerButton;
    Button getLocationButton;
    TextView locationText;
    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;
    GoogleMap mMap;
    private MapView mMapView;
    Marker marker;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    double Lat=0.00,Long=0.00;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemist_register);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.login_as_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        progressBar=(ProgressBar)findViewById(R.id.Progress);
        pharmaNameEdit=(EditText)findViewById(R.id.PharmaNameEdit);
        emailEdit=(EditText)findViewById(R.id.EmailEdit);
        passwordEdit=(EditText)findViewById(R.id.PasswordEdit);
        phoneEdit=(EditText)findViewById(R.id.PhoneEdit);
        loginButton=(Button) findViewById(R.id.LoginText);
        registerButton=(Button)findViewById(R.id.RegisterButton);
        getLocationButton=(Button)findViewById(R.id.GetLocationButton);
        locationText=(TextView)findViewById(R.id.LocationText);

        fStore=FirebaseFirestore.getInstance();
        fAuth=FirebaseAuth.getInstance();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.GetLocationMap);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    List<Address> listAddresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    Lat=location.getLatitude();
                    Long=location.getLongitude();
                    marker.setPosition(new LatLng(Lat,Long));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat,Long), 17));
                    if(listAddresses!=null&&listAddresses.size()>0){
                        locationText.setText(listAddresses.get(0).getAddressLine(0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChemistRegisterActivity.this, LoginAsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(ChemistRegisterActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChemistRegisterActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
                }else{
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,500,locationListener);
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String pharmaName=pharmaNameEdit.getText().toString();
                final String email=emailEdit.getText().toString().trim();
                String password=passwordEdit.getText().toString().trim();
                final String phone=phoneEdit.getText().toString();
                final String address=locationText.getText().toString();

                if(pharmaName.isEmpty()){
                    pharmaNameEdit.setError("Required");
                    return;
                }
                if(email.isEmpty()){
                    emailEdit.setError("Email Required");
                    return;
                }
                if(password.isEmpty()){
                    passwordEdit.setError("Password Required");
                    return;
                }
                if(password.length()<6){
                    passwordEdit.setError("Must be greater than 6 character");
                    return;
                }
                if(phone.isEmpty()){
                    phoneEdit.setError("Required");
                    return;
                }
                if(address.isEmpty()){
                    Toast.makeText(ChemistRegisterActivity.this, "Get Location to Register your Pharmacy", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                disableInputs();

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> chemist = new HashMap<>();
                            chemist.put("pharmaName",pharmaName);
                            chemist.put("email",email);
                            chemist.put("phone",phone);
                            chemist.put("address",address);
                            chemist.put("Lat",Lat);
                            chemist.put("Long",Long);

                            fStore.collection("Pharmacy").document(fAuth.getUid()).set(chemist).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ChemistRegisterActivity.this, "Pharmacy Registered", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(ChemistRegisterActivity.this, ChemistMainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    enableInputs();
                                    Toast.makeText(ChemistRegisterActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });

                        }
                        else{
                            enableInputs();
                            Exception e=task.getException();
                            e.printStackTrace();
                            Toast.makeText(ChemistRegisterActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(ChemistRegisterActivity.this)
                    .setMessage("GPS is required for this Action")
                    .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .show();
        }
    }

    private void disableInputs() {
        passwordEdit.setEnabled(false);
        pharmaNameEdit.setEnabled(false);
        emailEdit.setEnabled(false);
        phoneEdit.setEnabled(false);
        registerButton.setEnabled(false);
        loginButton.setEnabled(false);
    }

    private void enableInputs() {
        passwordEdit.setEnabled(true);
        pharmaNameEdit.setEnabled(true);
        emailEdit.setEnabled(true);
        phoneEdit.setEnabled(true);
        registerButton.setEnabled(true);
        loginButton.setEnabled(true);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED&& requestCode==101){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,500,locationListener);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap=map;
        marker=map.addMarker(new MarkerOptions().position(new LatLng(Lat, Long)).title("Your Location"));
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
