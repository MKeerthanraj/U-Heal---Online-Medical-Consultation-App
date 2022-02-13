package com.example.medicalconsultation.SymptomCheckerPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.medicalconsultation.R;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SymptomCheckerIssueDiagActivity extends AppCompatActivity {

    Button backButton;String html;
    String token,ID,gender,YOB;
    TextView symptomsTextView,descriptionTextView,medCondTextView,treatmentTextView,issueNameTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_checker_issue_diag);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        backButton=(Button)findViewById(R.id.BackButton);

        Bundle extras = getIntent().getExtras();
        String issueId = extras.getString("IssueId");
        ID=extras.getString("SymptomID");
        gender=extras.getString("Gender");
        YOB=extras.getString("YOB");
        token=extras.getString("token");

        symptomsTextView=(TextView)findViewById(R.id.symptomsText);
        descriptionTextView=(TextView)findViewById(R.id.descriptionText);
        medCondTextView=(TextView)findViewById(R.id.medicalCondText);
        treatmentTextView=(TextView)findViewById(R.id.treatmentDescriptionText);
        issueNameTextView=(TextView)findViewById(R.id.IssueNameText);

        html="https://sandbox-healthservice.priaid.ch/issues/"+issueId+"/info?token="+token+"&format=json&language=en-gb";

        downloadTask task = new downloadTask();

        try {
            String result = task.execute(html).get();
        }catch (Exception e){
            e.printStackTrace();
        }


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public class downloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                char current;
                String content;
                int data;
                StringBuilder builder = new StringBuilder();
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(in);
                data = isr.read();
                while (data != -1) {
                    current = (char) data;
                    builder.append(current);
                    data = isr.read();
                }
                content = builder.toString();
                return content;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject=new JSONObject(s);
                String isuueName=jsonObject.getString("ProfName");
                String possibleSymptoms=jsonObject.getString("PossibleSymptoms");
                String[] possibleSymptomsArray=possibleSymptoms.split(",");
                String description=jsonObject.getString("DescriptionShort");
                String medCond=jsonObject.getString("MedicalCondition");
                String treatment=jsonObject.getString("TreatmentDescription");

                String SymptomsInLines="";
                for(int i=0;i<possibleSymptomsArray.length;i++){
                    SymptomsInLines+=possibleSymptomsArray[i]+"\n";
                }

                issueNameTextView.setText("Issue Name : "+isuueName);
                symptomsTextView.setText(SymptomsInLines);
                descriptionTextView.setText(description);
                medCondTextView.setText(medCond);
                treatmentTextView.setText(treatment);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
