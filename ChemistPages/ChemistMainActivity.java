package com.example.medicalconsultation.ChemistPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;

import javax.annotation.Nullable;

public class ChemistMainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    TextView pharmaNameText,pharmaPhoneText,pharmaAddresstext;
    MapView mMapView;
    Button maintainStockButton;

    GoogleMap mMap;
    Marker marker;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    String pharmaName="Your Location";
    double Lat=0.00,Long=0.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemist_main);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.home_action_bar);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        pharmaNameText=(TextView)findViewById(R.id.PharmaNameText);
        pharmaPhoneText=(TextView)findViewById(R.id.PharmaPhoneText);
        pharmaAddresstext=(TextView)findViewById(R.id.PharmaAddressText);
        maintainStockButton=(Button)findViewById(R.id.MaintainStockButton);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.LocationMap);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);

        setUserInfo();

        maintainStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ChemistMainActivity.this,ChemistUpdateStockActivity.class));
            }
        });

    }

    public void logout(View view) {
        if (fAuth.getCurrentUser() != null) {
            fAuth.signOut();
            startActivity(new Intent(ChemistMainActivity.this, LoginAsActivity.class));
            finish();
        }
    }

    private void setUserInfo() {
        if(fAuth.getCurrentUser()!=null) {
            String uid = fAuth.getCurrentUser().getUid();
            DocumentReference userInfoDocumentReference = fStore.collection("Pharmacy").document(uid);
            userInfoDocumentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(e!=null)
                        e.printStackTrace();
                    else{
                        pharmaNameText.setText(documentSnapshot.getString("pharmaName"));
                        pharmaName=documentSnapshot.getString("pharmaName");
                        pharmaPhoneText.setText(documentSnapshot.getString("phone"));
                        pharmaAddresstext.setText(documentSnapshot.getString("address"));
                        Lat=documentSnapshot.getDouble("Lat");
                        Long=documentSnapshot.getDouble("Long");
                        marker.setPosition(new LatLng(Lat,Long));
                        marker.setTitle(pharmaName);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat,Long), 17));
                    }
                }
            });
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
        marker=map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title(pharmaName));
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