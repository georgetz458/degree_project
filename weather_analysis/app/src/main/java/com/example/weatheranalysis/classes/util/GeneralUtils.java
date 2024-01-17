package com.example.weatheranalysis.classes.util;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatheranalysis.R;

public class GeneralUtils {

    public static final String WORK_PREFERENCE_NAME = "work";
    public static final int NOTIFICATION_ID = 1;
    public static String getMainPreferencesName(Context context){
        return context.getString(R.string.preferences_name);
    }
    // μέθοδος για την προβολή μηνυμάτων
    public static void showMessage(Context context, String title, String message) {
        new AlertDialog.Builder(context).setTitle(title).setMessage(message).setCancelable(true).show();
    }



    public static void goToActivity( AppCompatActivity activity, Class<?> activityClass){

        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
        activity.finish();
    }


}
