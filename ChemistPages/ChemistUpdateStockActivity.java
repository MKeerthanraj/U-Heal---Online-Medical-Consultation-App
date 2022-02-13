package com.example.medicalconsultation.ChemistPages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.medicalconsultation.DoctorPages.DoctorMainAcitivty;
import com.example.medicalconsultation.LoginPages.DoctorLoginActivity;
import com.example.medicalconsultation.LoginPages.LoginAsActivity;
import com.example.medicalconsultation.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChemistUpdateStockActivity extends AppCompatActivity {

    Button backButton,addMedicineButton,saveDataButton;
    EditText medicineNameEdit;
    LinearLayout stockListLinearLayout;
    ArrayList<String> availabilityList;
    ArrayAdapter<String> availabilitySpinnerMainAdapter;
    Spinner availabilitySpinnerMain;
    ArrayList<String> medicineNameList;
    ArrayList<Long> medicineAvailableList;
    ProgressBar progressBar;

    String medicineName;
    int avail;

    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chemist_update_stock);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        addMedicineButton=(Button)findViewById(R.id.AddMedicineButton);
        saveDataButton=(Button)findViewById(R.id.SaveDataButton);
        medicineNameEdit=(EditText)findViewById(R.id.MedicineNameEdit);
        stockListLinearLayout=(LinearLayout)findViewById(R.id.StockListLinearLayout);
        availabilitySpinnerMain=(Spinner)findViewById(R.id.AvailabilitySpinnerMain);
        progressBar=(ProgressBar)findViewById(R.id.Progress);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        availabilityList=new ArrayList<>();
        availabilityList.add("Available");
        availabilityList.add("Not Available");

        medicineNameList=new ArrayList<>();
        medicineAvailableList=new ArrayList<>();

        availabilitySpinnerMainAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1,availabilityList);

        availabilitySpinnerMain.setAdapter(availabilitySpinnerMainAdapter);
        availabilitySpinnerMain.setSelection(0);

        UpdateAvailableStock();

        addMedicineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(medicineNameEdit.getText().length()!=0){
                    saveDataButton.setEnabled(true);
                    backButton.setEnabled(false);
                    Toast.makeText(ChemistUpdateStockActivity.this, "Don't Forget to Save Changes!!", Toast.LENGTH_SHORT).show();
                    String stockName=medicineNameEdit.getText().toString();
                    medicineName=stockName;
                    avail=availabilitySpinnerMain.getSelectedItemPosition();
                    medicineNameList.add(stockName);
                    medicineAvailableList.add((long) availabilitySpinnerMain.getSelectedItemPosition());
                    addView(stockName,availabilitySpinnerMain.getSelectedItemPosition());
                    medicineNameEdit.setText(null);
                    availabilitySpinnerMain.setSelection(0);
                }else
                    Toast.makeText(ChemistUpdateStockActivity.this, "Enter Medicine Name", Toast.LENGTH_SHORT).show();
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

    private void UpdateAvailableStock() {
        if(fAuth.getCurrentUser()!=null){
            progressBar.setVisibility(View.VISIBLE);
            fStore.collection("Pharmacy").document(fAuth.getUid()).collection("Stock").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot DOCUMENT : task.getResult()) {
                            medicineNameList.add(DOCUMENT.getId());
                            medicineAvailableList.add(DOCUMENT.getLong("availability"));
                            if(DOCUMENT.getLong("availability")==0)
                                addView(DOCUMENT.getId(),0);
                            else if(DOCUMENT.getLong("availability")==1)
                                addView(DOCUMENT.getId(),1);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public void SaveData(View view){
        if(fAuth.getCurrentUser()!=null){
            progressBar.setVisibility(View.VISIBLE);
            for(int i=0;i<medicineNameList.size();i++){
                Map<String,Object> availabilityMap= new HashMap<>();
                availabilityMap.put("availability",medicineAvailableList.get(i));
                fStore.collection("Pharmacy").document(fAuth.getUid()).collection("Stock").document(medicineNameList.get(i)).set(availabilityMap);
            }
            progressBar.setVisibility(View.GONE);
            saveDataButton.setEnabled(false);
            backButton.setEnabled(true);
            Toast.makeText(ChemistUpdateStockActivity.this, "Changes Saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void addView(String stockName,int selection) {
        View listElement=getLayoutInflater().inflate(R.layout.stock_list_element,null,false);
        View spaceBetween=getLayoutInflater().inflate(R.layout.space_between_stock,null,false);
        TextView medicineText=(TextView)listElement.findViewById(R.id.MedicineNameText);
        Button removeMedicine=(Button) listElement.findViewById(R.id.RemoveStockButton);
        Spinner availabilitySpinner=(Spinner)listElement.findViewById(R.id.AvailabilitySpinner);
        ImageView availabityBox=(ImageView)listElement.findViewById(R.id.Check_box);

        medicineText.setText(stockName);
        availabilitySpinner.setAdapter(availabilitySpinnerMainAdapter);
        availabilitySpinner.setSelection(selection);

        if(availabilitySpinner.getSelectedItemPosition()==0)
            availabityBox.setBackground(getDrawable(R.drawable.availability_green_box));
        else if(availabilitySpinner.getSelectedItemPosition()==1)
            availabityBox.setBackground(getDrawable(R.drawable.availability_red_box));

        availabilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int index=medicineNameList.indexOf(medicineText.getText().toString());
                medicineAvailableList.set(index,(long)position);
                if(position==0)
                    availabityBox.setBackground(getDrawable(R.drawable.availability_green_box));
                else if(position==1)
                    availabityBox.setBackground(getDrawable(R.drawable.availability_red_box));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        stockListLinearLayout.addView(listElement);
        stockListLinearLayout.addView(spaceBetween);

        removeMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index=medicineNameList.indexOf(medicineText.getText().toString());
                String stock=medicineNameList.get(index);
                medicineNameList.remove(index);
                medicineAvailableList.remove(index);
                removeData(stock);
                listElement.animate().translationXBy(-600).setDuration(500);
                (new Handler()).postDelayed(this::RemoveViews,500);
            }

            private void removeData(String stock) {
                if(fAuth.getCurrentUser()!=null)
                    fStore.collection("Pharmacy").document(fAuth.getUid()).collection("Stock").document(stock).delete();
            }

            private void RemoveViews() {
                stockListLinearLayout.removeView(listElement);
                stockListLinearLayout.removeView(spaceBetween);
            }
        });
    }
}