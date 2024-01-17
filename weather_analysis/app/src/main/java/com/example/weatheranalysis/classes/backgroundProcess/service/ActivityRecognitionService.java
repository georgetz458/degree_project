package com.example.weatheranalysis.classes.backgroundProcess.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;

import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.WeatherUtils;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;
//service που εκτελείται όταν το ActivityRecognition API δει Activity update
public class ActivityRecognitionService extends Service {


    private Context context;
    private WeatherUtils weatherUtils;
    public static boolean Active = false;
    private boolean isOutdoors;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



            Active = true;
            context = getApplicationContext();




            SharedPreferences actRecPreferences = getApplicationContext().getSharedPreferences("ActRec", Context.MODE_PRIVATE);

            double light = actRecPreferences.getFloat("light", 0);
            double phoneTemp = actRecPreferences.getFloat("phoneTemp", 0);
            double onlineTemp = actRecPreferences.getFloat("onlineTemp", 0);
            String preferences_name = actRecPreferences.getString("preferences_name", "NONE");
            SharedPreferences.Editor actRecPreferencesEditor = actRecPreferences.edit();
            actRecPreferencesEditor.remove("light");
            actRecPreferencesEditor.remove("phoneTemp");
            actRecPreferencesEditor.remove("onlineTemp");
            actRecPreferencesEditor.remove("preferences_name");
            actRecPreferencesEditor.putBoolean("ActRec", true);
            actRecPreferencesEditor.apply();


            weatherUtils = new WeatherUtils(getApplicationContext(), preferences_name);
            initNewWork(intent, light, phoneTemp, onlineTemp);
            return START_STICKY;


    }


    //δρομολόγηση νέου work
    private void initNewWork(Intent intent, double light, double phoneTemp, double onlineTemp) {
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(getString(R.string.worker_tag_name));


        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        //λίστα με activities με confidence, όσο ποιο υψηλο το confidence τόσο ποιο πιθανό να έιναι αυτή η δραστηριότητα
        //έιναι ταξηνομημένη από τη ποιο πιθανή στη λιγότερο πιθανή
        List<DetectedActivity> activities = result.getProbableActivities();

        int activity = activities.get(0).getType();
        int confidence = activities.get(0).getConfidence();
        if(confidence >=50 ){
            isOutdoors = weatherUtils.isOutdoors(onlineTemp, phoneTemp, light, activity);
        }else {
            isOutdoors = weatherUtils.isOutdoors(onlineTemp, phoneTemp, light);
        }


        weatherUtils.enqueueUploadWork(isOutdoors);
        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            MySingletonNotificationManager.getInstance(getApplicationContext())
                    .setNotification(getString(R.string.activity_rec_permission_lost_title), getString(R.string.activity_rec_permission_lost_content));

            return;
        }
        ActivityRecognition.getClient(this).removeActivityUpdates(pendingIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        UploadService.ActRecEnded = true;
        Active = false;


        SharedPreferences actRecPreferences = getApplicationContext().getSharedPreferences("ActRec", Context.MODE_PRIVATE);
        actRecPreferences.edit()
                .remove("ActRec")
                .apply();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }




}
