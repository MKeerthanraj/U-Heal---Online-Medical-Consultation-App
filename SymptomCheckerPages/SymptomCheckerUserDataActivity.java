package com.example.medicalconsultation.SymptomCheckerPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SymptomCheckerUserDataActivity extends AppCompatActivity {

    Button backButton;
    HttpURLConnection connection;
    URL url;
    ListView SymptomNameListView;
    ArrayList<String> SymptomNameFullArrayList,SymptomNameSearchArrayList,SymptomIDArrayList;
    ArrayAdapter<String> arrayAdapter;
    EditText searchEditText;
    String token="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_checker_user_data);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        backButton=(Button)findViewById(R.id.BackButton);

        SymptomNameListView=(ListView)findViewById(R.id.listView);
        SymptomNameListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        final RadioButton MaleRadio=(RadioButton)findViewById(R.id.MaleRadio);
        final RadioButton FemaleRadio=(RadioButton)findViewById(R.id.FemaleRadio);
        final EditText YOBedit=(EditText)findViewById(R.id.YOBEditText);

        SymptomNameFullArrayList=new ArrayList<String>();
        SymptomNameSearchArrayList=new ArrayList<String>();
        SymptomIDArrayList=new ArrayList<String>();

        arrayAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,SymptomNameSearchArrayList);
        SymptomNameListView.setAdapter(arrayAdapter);

        searchEditText=(EditText)findViewById(R.id.editText);

        downloadTask task=new downloadTask();
        downloadKey taskKey=new downloadKey();

        try{
            String keyJson=taskKey.execute("https://us-central1-symptom-checker-apikey.cloudfunctions.net/app/api/read").get();
            JSONArray array=new JSONArray(keyJson);
            JSONObject element=array.getJSONObject(0);
            token=element.getString("Token");
        }catch (Exception e){
            e.printStackTrace();
        }

        String html="https://sandbox-healthservice.priaid.ch/symptoms?token="+token+"&format=json&language=en-gb";

        try{
            String result=task.execute(html).get();
        }catch (Exception e){
            e.printStackTrace();
        }

        SymptomNameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((MaleRadio.isChecked() || FemaleRadio.isChecked()) && !(YOBedit.getText().toString().isEmpty())) {
                    String Symptom = SymptomNameSearchArrayList.get(position);
                    Symptom = Symptom.toLowerCase();
                    for (int i = 0; i < SymptomNameFullArrayList.size(); i++) {
                        if (SymptomNameFullArrayList.get(i).toLowerCase().startsWith(Symptom)) {
                            String ID = SymptomIDArrayList.get(i);
                            String gender = "";
                            String YOB = YOBedit.getText().toString();
                            if (MaleRadio.isChecked())
                                gender = "male";
                            else if (FemaleRadio.isChecked())
                                gender = "female";
                            Intent intent = new Intent(SymptomCheckerUserDataActivity.this, SymptomCheckerDiagActivity.class);
                            intent.putExtra("SymptomID", ID);
                            intent.putExtra("Gender", gender);
                            intent.putExtra("YOB", YOB);
                            intent.putExtra("token",token);
                            startActivity(intent);
                        }
                    }
                }
                else
                    Toast.makeText(SymptomCheckerUserDataActivity.this, "Enter all Required Fields", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SymptomNameSearchArrayList.clear();
                arrayAdapter.notifyDataSetChanged();
                String word,searchQuery=s.toString();
                searchQuery=searchQuery.toLowerCase();
                for(int i=0;i<SymptomNameFullArrayList.size();i++){
                    word=SymptomNameFullArrayList.get(i);
                    word=word.toLowerCase();
                    if(word.startsWith(searchQuery))
                        SymptomNameSearchArrayList.add(word);
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SymptomCheckerUserDataActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    public class downloadKey extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder builder=new StringBuilder();
            char current;
            int data;
            String content;
            try {
                url=new URL(urls[0]);
                connection=(HttpURLConnection)url.openConnection();
                InputStream in =connection.getInputStream();
                InputStreamReader isr=new InputStreamReader(in);
                data=isr.read();
                while (data!=-1){
                    current=(char)data;
                    builder.append(current);
                    data=isr.read();
                }
                content=builder.toString();
                return content;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

    }


    public class downloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            StringBuilder builder=new StringBuilder();
            char current;
            int data;
            String content;
            try{
                url=new URL(urls[0]);
                connection=(HttpURLConnection)url.openConnection();
                InputStream in =connection.getInputStream();
                InputStreamReader isr=new InputStreamReader(in);
                data=isr.read();
                while(data!=-1){
                    current=(char) data;
                    builder.append(current);
                    data=isr.read();
                }
                content=builder.toString();
                return content;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try{
                JSONArray array=new JSONArray(s);
                for(int i=0;i<array.length();i++){
                    JSONObject element=array.getJSONObject(i);
                    SymptomNameFullArrayList.add(element.getString("Name"));
                    SymptomIDArrayList.add(element.getString("ID"));
                }
                SymptomNameSearchArrayList.addAll(SymptomNameFullArrayList);
                arrayAdapter.notifyDataSetChanged();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
