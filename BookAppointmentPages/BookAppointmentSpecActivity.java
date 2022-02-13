package com.example.medicalconsultation.BookAppointmentPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;
import com.example.medicalconsultation.SpecArrayListClass;

import java.util.ArrayList;

public class BookAppointmentSpecActivity extends AppCompatActivity {

    Button backButton;
    EditText searchSpecEdit;
    ListView allSpecListView;
    ArrayList<String> specArrayList,searchSpecArrayList;
    ArrayAdapter<String> specArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment_spec);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        backButton=(Button)findViewById(R.id.BackButton);
        searchSpecEdit=(EditText)findViewById(R.id.SearchSpecEdit);
        allSpecListView=(ListView)findViewById(R.id.AllSpecListView);

        specArrayList=new ArrayList<String>();
        searchSpecArrayList=new ArrayList<String>();

        SpecArrayListClass specArrayListClass=new SpecArrayListClass();
        specArrayListClass.setSpecList();
        specArrayList.addAll(specArrayListClass.getSpecList());
        specArrayList.remove(0);
        searchSpecArrayList.addAll(specArrayList);
        specArrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,searchSpecArrayList);
        allSpecListView.setAdapter(specArrayAdapter);
        specArrayAdapter.notifyDataSetChanged();

        searchSpecEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("")){
                    searchSpecArrayList.clear();
                    searchSpecArrayList.addAll(specArrayList);
                }
                else {
                    searchSpecArrayList.clear();
                    specArrayAdapter.notifyDataSetChanged();
                    String word, searchQuery = s.toString();
                    String upperString = searchQuery.substring(0, 1).toUpperCase() + searchQuery.substring(1).toLowerCase();
                    for (int i = 0; i < specArrayList.size(); i++) {
                        word = specArrayList.get(i);
                        if (word.startsWith(upperString))
                            searchSpecArrayList.add(word);
                    }
                }
                specArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        allSpecListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(BookAppointmentSpecActivity.this,BookAppointmentDocSelectActivity.class);
                intent.putExtra("Spec",specArrayList.get(position));
                startActivity(intent);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BookAppointmentSpecActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
