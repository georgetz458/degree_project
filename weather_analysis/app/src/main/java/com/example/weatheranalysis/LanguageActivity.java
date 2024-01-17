
package com.example.weatheranalysis;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.weatheranalysis.classes.MySignedActivityTemplate;
//activity εναλλαγής γλώσσας
public class LanguageActivity extends MySignedActivityTemplate {
    private Spinner spinner;
    private SharedPreferences languagePref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        spinner =  (Spinner) findViewById(R.id.language_spinner);
        languagePref = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        setLanguage();
        setMenu();

    }

    private void setLanguage(){
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.languages
                , androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        // εάν το locale είναι ελληνικό δηλαδή el θέτουμε στη λίστα η επιλεγμένη τιμή να είναι τα ελληνικά
        String locale = languagePref.getString("language", "en");
        
        if (locale.equals("el")) {
            spinner.setSelection(1);
        }
        else{
            spinner.setSelection(0);
        }
    }
    public void change(View view){
        String language = spinner.getSelectedItem().toString();
        String locale;
        if(language.equals(getString(R.string.greek))){
            locale = "el";
        }
        else{
            locale = "en";
        }

            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(locale);

            AppCompatDelegate.setApplicationLocales(appLocale);

        languagePref.edit()
            .putString("language", locale)
            .apply();
        // κλήση μεθόδου reloadActivity() για να φανούν οι αλλαγές
        reloadActivity();

    }

    // μέθοδος για επαναφόρτωση του activity
    private void reloadActivity() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }



    @Override
    protected boolean homeButtonAction() {
        startMainActivity();
        return false;
    }

    @Override
    protected boolean infoButtonAction() {
        startInfoActivity();
        return true;
    }

    @Override
    protected boolean languageButtonAction() {
        closeDrawer();
        return false;
    }
}