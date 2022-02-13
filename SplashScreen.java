package com.example.medicalconsultation;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.medicalconsultation.LoginPages.LoginAsActivity;

public class SplashScreen extends AppCompatActivity {

    ImageView pinkBox,greenBox;
    ProgressBar progressBar;
    TextView medText,appText;
    Animation pinkBoxAnim,medTextIntroAnimation,APPTextIntroAnimation,pinkBoxFadeOutAnimation,greenBoxAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.login_as_action_bar_layout);
        if(Build.VERSION.SDK_INT>=23){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        pinkBox=(ImageView)findViewById(R.id.pinkBox);
        greenBox=(ImageView)findViewById(R.id.GreenBox);
        medText=(TextView)findViewById(R.id.MedText);
        progressBar=(ProgressBar)findViewById(R.id.Progress);
        appText=(TextView)findViewById(R.id.APPText);
        pinkBoxAnim= AnimationUtils.loadAnimation(this,R.anim.pink_box_anim);
        medTextIntroAnimation=AnimationUtils.loadAnimation(this,R.anim.med_text_intro_anim);
        APPTextIntroAnimation=AnimationUtils.loadAnimation(this,R.anim.app_text_intro_anim);
        pinkBoxFadeOutAnimation=AnimationUtils.loadAnimation(this,R.anim.pink_out_anim);
        greenBoxAnimation=AnimationUtils.loadAnimation(this,R.anim.green_box_anim);

        medText.setAlpha(0);
        appText.setAlpha(0);
        greenBox.setVisibility(View.INVISIBLE);

        pinkBox.setAnimation(pinkBoxAnim);
        (new Handler()).postDelayed(this::TextIntroAnim,500);
    }

    private void TextIntroAnim() {
        medText.setAlpha(1);
        appText.setAlpha(1);
        medText.setAnimation(medTextIntroAnimation);
        appText.setAnimation(APPTextIntroAnimation);
        (new Handler()).postDelayed(this::pinkBoxFadeOut,300);
    }

    private void pinkBoxFadeOut(){
        pinkBox.setAnimation(pinkBoxFadeOutAnimation);
        (new Handler()).postDelayed(this::pinkBoxGone,500);
    }

    private void pinkBoxGone(){
        pinkBox.setVisibility(View.GONE);
        greenBoxAnim();
    }

    private void greenBoxAnim() {
        greenBox.setVisibility(View.VISIBLE);
        greenBox.setAnimation(greenBoxAnimation);
        (new Handler()).postDelayed(this::pinkBoxScale,400);
    }

    private void pinkBoxScale() {
        greenBox.animate().scaleX((float) 1.8).setDuration(400);
        progressBar.setVisibility(View.VISIBLE);
        (new Handler()).postDelayed(this::openApp,2000);
    }

    private void openApp() {
        startActivity(new Intent(SplashScreen.this, LoginAsActivity.class));
        finish();
    }
}
