package com.example.medicalconsultation.SearchMedicinePages;

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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.DoctorPages.DoctorMainAcitivty;
import com.example.medicalconsultation.LoginPages.DoctorLoginActivity;
import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.RegisterPages.ChemistRegisterActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public class SearchMedicineMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    MapView mMapView;
    GoogleMap mMap;
    Marker marker;
    LocationManager locationManager;
    LocationListener locationListener;
    Geocoder geocoder;
    ProgressBar progressBar;

    Button backButton,searchButton;
    EditText searchMedicineEdit;
    double Lat=0.00,Long=0.00;
    LatLng ll1;

    LinearLayout pharmacyListLayout;
    ScrollView scrollView;
    ArrayList<String> pharmaNameArrayList;
    ArrayList<Double> pharmaDistArrayList;
    ArrayList<LatLng> pharmaLatLongArrayList;

    FirebaseFirestore fStore;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_medicine_map);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        backButton=(Button)findViewById(R.id.BackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchButton=(Button)findViewById(R.id.SearchButton);
        searchMedicineEdit=(EditText)findViewById(R.id.SearchMedicineEdit);
        pharmacyListLayout=(LinearLayout)findViewById(R.id.PharmacyListLinearLayout);
        scrollView=(ScrollView)findViewById(R.id.scrollView);
        progressBar=(ProgressBar)findViewById(R.id.Progress);

        pharmaNameArrayList=new ArrayList<String>();
        pharmaDistArrayList=new ArrayList<Double>();
        pharmaLatLongArrayList=new ArrayList<LatLng>();

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.MAP);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());

        ll1=new LatLng(Lat,Long);

        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    List<Address> listAddresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    Lat=location.getLatitude();
                    Long=location.getLongitude();
                    ll1=new LatLng(Lat,Long);
                    marker.setPosition(new LatLng(Lat,Long));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat,Long), 17));
                    progressBar.setVisibility(View.GONE);
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
        if(ContextCompat.checkSelfPermission(SearchMedicineMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SearchMedicineMapActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},101);
        }else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,500,locationListener);
        }

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(SearchMedicineMapActivity.this)
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

    public void SearchNearBy(View view){
        progressBar.setVisibility(View.VISIBLE);
        if(searchMedicineEdit.getText().length()>0){
            pharmaNameArrayList.clear();
            pharmaDistArrayList.clear();
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(ll1).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) );
            String medicine=searchMedicineEdit.getText().toString().toLowerCase();
            if(fAuth.getCurrentUser()!=null){
                fStore.collection("Pharmacy").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> nearByPharmaList=new ArrayList<>();
                            for (QueryDocumentSnapshot DOCUMENT : task.getResult()) {
                                double newLat=DOCUMENT.getDouble("Lat");
                                double newLong=DOCUMENT.getDouble("Long");
                                LatLng ll2=new LatLng(newLat,newLong);
                                double distance = SphericalUtil.computeDistanceBetween(ll1, ll2);
                                if(distance/1000<5){
                                    nearByPharmaList.add(DOCUMENT.getId());
                                }
                            }
                            for(int i=0;i<nearByPharmaList.size();i++){
                                checkForPharmacy(nearByPharmaList,i,medicine);
                            }
                        }
                    }
                });
            }
        }
    }

    private void checkForPharmacy(ArrayList<String> nearByPharmaList, int i,String medicine) {
        fStore.collection("Pharmacy").document(nearByPharmaList.get(i)).collection("Stock").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                if(task1.isSuccessful()){
                    for(QueryDocumentSnapshot DOC : task1.getResult()){
                        if(DOC.getId().toLowerCase().contains(medicine)){
                            if(DOC.getLong("availability")==0){
                                fStore.collection("Pharmacy").document(nearByPharmaList.get(i)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                        if(e!=null)
                                            e.printStackTrace();
                                        else{
                                            LatLng ll2=new LatLng(documentSnapshot.getDouble("Lat"),documentSnapshot.getDouble("Long"));
                                            String PharmaName=documentSnapshot.getString("pharmaName");
                                            double distance=SphericalUtil.computeDistanceBetween(ll1, ll2);
                                            mMap.addMarker(new MarkerOptions().position(ll2).title(PharmaName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                            progressBar.setVisibility(View.GONE);
                                            pharmaNameArrayList.add(PharmaName);
                                            pharmaDistArrayList.add(distance);
                                            pharmaLatLongArrayList.add(ll2);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }


    private void addViewToLayout(String name, double dist,LatLng llt) {
        View listElement=getLayoutInflater().inflate(R.layout.near_by_pharmacies_layout,null,false);
        View spaceBetween=getLayoutInflater().inflate(R.layout.space_between_stock,null,false);
        TextView pharmaNameTextView=(TextView)listElement.findViewById(R.id.PharmacyNameText);
        TextView pharmaDistTextView=(TextView)listElement.findViewById(R.id.PharmacyDistanceText);

        pharmaNameTextView.setText(name);
        pharmaDistTextView.setText(String.valueOf(Math.round((dist/1000) * 100.0) / 100.0)+" km");

        listElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(llt, 17));
            }
        });

        pharmacyListLayout.addView(listElement);
        pharmacyListLayout.addView(spaceBetween);
    }

    public void ShowList(View view){

        if(view.getRotation()==-90){
            view.animate().rotationBy(180);
            searchMedicineEdit.setEnabled(false);
            searchButton.setEnabled(false);
            for(int i=0;i<pharmaNameArrayList.size();i++){
                String name=pharmaNameArrayList.get(i);
                double dist=pharmaDistArrayList.get(i);
                LatLng llt=pharmaLatLongArrayList.get(i);
                addViewToLayout(name,dist,llt);
            }
        }
        else if(view.getRotation()==90){
            view.animate().rotationBy(-180);
            pharmacyListLayout.removeAllViews();
            searchMedicineEdit.setEnabled(true);
            searchButton.setEnabled(true);
        }
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
        marker=map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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