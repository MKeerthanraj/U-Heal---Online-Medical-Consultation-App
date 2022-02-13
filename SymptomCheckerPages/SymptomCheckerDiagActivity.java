package com.example.medicalconsultation.SymptomCheckerPages;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.medicalconsultation.MainActivity;
import com.example.medicalconsultation.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SymptomCheckerDiagActivity extends AppCompatActivity {

    Button backButton;
    String html;
    String ID,gender,YOB;
    String token;
    LinearLayout addCardList;
    int[] drawableBGArray;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_checker_diag);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.back_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        backButton=(Button)findViewById(R.id.BackButton);

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Loading");
        dialog.setMessage("We are Gathering related Issues");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        drawableBGArray = new int[]{R.drawable.card_bg_1, R.drawable.card_bg_2,R.drawable.card_bg_3,R.drawable.card_bg_4};

        addCardList=findViewById(R.id.addCardList);
        Bundle extras=getIntent().getExtras();
        ID=extras.getString("SymptomID");
        gender=extras.getString("Gender");
        YOB=extras.getString("YOB");
        token=extras.getString("token");

        html="https://sandbox-healthservice.priaid.ch/diagnosis?symptoms=["+ID+"]&gender="+gender+"&year_of_birth="+YOB+"&token="+token+"&format=json&language=en-gb";

        downloadTask task=new downloadTask();

        try {
            String result=task.execute(html).get();
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

    public void addCard(final String issueId, String ProfName, String symptom, ArrayList<String> specArrayList, int j){
        View cardView=getLayoutInflater().inflate(R.layout.diag_symptom_card,null,false);
        View spaceBetween=getLayoutInflater().inflate(R.layout.space_between_cards,null,false);
        TextView issueNameText=(TextView)cardView.findViewById(R.id.IssueNameText);
        TextView symptomNameText=(TextView)cardView.findViewById(R.id.SymptomNameText);
        TextView specText=(TextView)cardView.findViewById(R.id.SpecText);
        for(int i=0;i<specArrayList.size();i++){
            specText.append(specArrayList.get(i)+"\n");
        }
        issueNameText.setText("Issue : "+ProfName);
        symptomNameText.setText(symptom);

        if(j<4)
            cardView.setBackgroundResource(drawableBGArray[j]);
        else if(j>=4)
            cardView.setBackgroundResource(drawableBGArray[j-4]);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SymptomCheckerDiagActivity.this,SymptomCheckerIssueDiagActivity.class);
                intent.putExtra("IssueId",issueId);
                intent.putExtra("SymptomID", ID);
                intent.putExtra("Gender", gender);
                intent.putExtra("YOB", YOB);
                intent.putExtra("token",token);
                startActivity(intent);
            }
        });

        addCardList.addView(cardView);
        addCardList.addView(spaceBetween);
    }


    public class downloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            try{
                char current;
                String content;
                int data;
                StringBuilder builder=new StringBuilder();
                URL url=new URL(urls[0]);
                HttpURLConnection connection=(HttpURLConnection)url.openConnection();
                InputStream in = connection.getInputStream();
                InputStreamReader isr=new InputStreamReader(in);
                data=isr.read();
                while(data!=-1){
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                ArrayList<String> specArrayList=new ArrayList<String>();
                JSONArray fullArray=new JSONArray(s);
                for(int i =0;i<fullArray.length();i++){
                    specArrayList.clear();
                    JSONObject DiagObject=fullArray.getJSONObject(i);
                    JSONObject issueObject=DiagObject.getJSONObject("Issue");
                    String issueId=issueObject.getString("ID");
                    String ProfName=issueObject.getString("ProfName");
                    String symptom=issueObject.getString("Name");
                    JSONArray specArray=DiagObject.getJSONArray("Specialisation");
                    for(int j=0;j<specArray.length();j++){
                        JSONObject specObject=specArray.getJSONObject(j);
                        String specName=specObject.getString("Name");
                        specArrayList.add(specName);
                    }
                    addCard(issueId,ProfName,symptom,specArrayList,i);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();

        }
    }

}
