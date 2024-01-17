package com.example.weatheranalysis;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.weatheranalysis.classes.MySignedActivityTemplate;
import com.example.weatheranalysis.classes.util.GeneralUtils;
import com.google.firebase.auth.FirebaseAuth;
//activity για πληροφορίες
public class InfoActivity extends MySignedActivityTemplate {
    private Spinner spinner;
    private TextView infoTextView;
    private FirebaseAuth mAUth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        infoTextView = findViewById(R.id.info_text);
        spinner = findViewById(R.id.info_spinner);

        setSpinner();
        mAUth = FirebaseAuth.getInstance();
        if(mAUth.getCurrentUser() != null){
            setMenu();
        }



    }

    private void setSpinner(){
        ArrayAdapter<CharSequence> arrayAdapter =  ArrayAdapter.createFromResource(this, R.array.info_array
                , androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
                String info = parent.getItemAtPosition(pos).toString();
                if(info.equals(getString(R.string.general_info_title))){
                    infoTextView.setText(R.string.general_info_prompt);
                }
                else if(info.equals(getString(R.string.chinese_phone_info))){
                    infoTextView.setText(R.string.chinese_phone_info_prompt);
                }
                
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected boolean homeButtonAction() {
        startMainActivity();
        return true;
    }

    @Override
    protected boolean infoButtonAction() {
        closeDrawer();
        return false;
    }

    @Override
    protected boolean languageButtonAction() {
        startLanguageActivity();
        return true;
    }

    private void startLogin(){
        GeneralUtils.goToActivity(this, LoginActivity.class);
    }
    public void start(View view){
        if(mAUth.getCurrentUser() !=null){
            startMainActivity();
        }else{
            startLogin();
        }

    }

}