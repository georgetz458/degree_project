package com.example.weatheranalysis.classes.backgroundProcess.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;
import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.GeneralUtils;
import com.example.weatheranalysis.classes.util.WeatherUtils;
import com.example.weatheranalysis.classes.repository.WeatherSensorRepository;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.google.android.gms.location.ActivityRecognition;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
//service για τη συλλογή δεδομένων αισθητήρων και την αποστολή δεδομένων στο backend
public class UploadService extends Service {

    public static boolean ActRecEnded = false;
    public static boolean Active = false;
    private SharedPreferences sharedPreferences;
    private WeatherSensorRepository repository;
    private WeatherUtils weatherUtils;
    private Intent actRecIntent;
    private PendingIntent actRecPendingIntent;


    public UploadService() {
    }

    private void init(String loginToken) {


        SensorManager sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        String preferences_name = getApplicationContext().getResources().getString(R.string.preferences_name);
        sharedPreferences = getApplicationContext().getSharedPreferences(preferences_name, Context.MODE_PRIVATE);


        repository = new WeatherSensorRepository(getApplicationContext(), sensorManager, this, preferences_name, loginToken);
        weatherUtils = new WeatherUtils(getApplicationContext(), preferences_name);
        sharedPreferences.edit()
                .putBoolean("AlarmServiceRunning", true)
                .apply();

    }
    //απόκτηση δεδομένων αισθητήρων και αποστολή όλων των δεδομένων μέσω αντικειμένου WeatherSensorRepository
    private void act(JSONObject owResponse, double batteryTemp, String loginToken) {
        init(loginToken);

        repository.getSensorDataAndSendData(owResponse, batteryTemp);
        stopForeground(true);

    }

    //εκτελείται κατά την εκκίνηση του service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Active = true;

        startForeground(GeneralUtils.NOTIFICATION_ID,
                MySingletonNotificationManager.getInstance(getApplicationContext())
                        .setNotification("Sensor Data", "Retrieving"));

        String owResponseString = intent.getStringExtra("owResponse");
        JSONObject owResponse;
        try {
            owResponse = new JSONObject(owResponseString);
            double batteryTemp = intent.getDoubleExtra("batteryTemp", 0);
            String loginToken = intent.getStringExtra("loginToken");
            //καλείται η act
            act(owResponse, batteryTemp, loginToken);
        } catch (JSONException e) {

            MySingletonNotificationManager.getInstance(getApplicationContext())
                    .setNotification(getString(R.string.error), getString(R.string.work_failure));

        }

        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        //Log.d("Important", "Destroyed service!");
        sharedPreferences.edit()
                .remove("AlarmServiceRunning")
                .apply();

        repository.getSensorManager().unregisterListener(repository.sensorEventListenerPressure);
        repository.getSensorManager().unregisterListener(repository.sensorEventListenerLight);
        repository.getSensorManager().unregisterListener(repository.sensorEventListenerProximity);
        Active = false;

    }

    //δρομολόγηση νέου work
    public void initNewWork(boolean isOutdoors,double phoneTemp,double onlineTemp, double light, String preferences_name)  {
        actRecIntent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
        SharedPreferences actRecPreferences = getApplicationContext().getSharedPreferences("ActRec", Context.MODE_PRIVATE);
        SharedPreferences.Editor actRecPreferencesEditor = actRecPreferences.edit();
        actRecPreferencesEditor.putFloat("light", (float) light);
        actRecPreferencesEditor.putFloat("phoneTemp", (float) phoneTemp);
        actRecPreferencesEditor.putFloat("onlineTemp", (float) onlineTemp);
        actRecPreferencesEditor.putString("preferences_name", preferences_name);
        actRecPreferencesEditor.apply();


        actRecPendingIntent = PendingIntent.getService(getApplicationContext(), 0, actRecIntent, PendingIntent.FLAG_MUTABLE);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            MySingletonNotificationManager.getInstance(getApplicationContext())
                    .setNotification(getString(R.string.activity_rec_permission_lost_title), getString(R.string.activity_rec_permission_lost_content));

            weatherUtils.enqueueUploadWork(isOutdoors);
            stopSelf();
            return;
        }
        sharedPreferences.edit()
                .putBoolean("isActRecIntent", true)
                .putBoolean("isActRecPendingIntent", true)
                .apply();
        //αίτηση για Activity Updates μέσω ActivityRecognition
        //εάν εκτελεστεί τότε η δρομολόγηση θα γίνει εκεί
        ActivityRecognition.getClient(getApplicationContext()).requestActivityUpdates(1000, actRecPendingIntent);
        //αναμονή για την πιθανή εκτέλεση στο ActivityRecognitionService

          ExecutorService executorService = Executors.newFixedThreadPool(2);


        Future<Boolean> future = executorService.submit(()->{//anonymous Callable
            while (true){
                if(ActRecEnded){
                    ActRecEnded = false;
                    return true;
                }
            }
        });
        executorService.submit(()->{
            try{
                future.get(20, TimeUnit.SECONDS);

                stopSelf();
                executorService.shutdown();
            }catch (TimeoutException | ExecutionException | InterruptedException e){
                ActRecEnded = false;
                ActivityRecognition.getClient(getApplicationContext()).removeActivityUpdates(actRecPendingIntent);
                getApplicationContext().stopService(actRecIntent);

                    WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(getApplicationContext().getString(R.string.worker_tag_name));
                    weatherUtils.enqueueUploadWork(isOutdoors);
                    stopSelf();
                    executorService.shutdown();

            }
        });


    }

    public WeatherUtils getWeatherUtils() {
        return weatherUtils;
    }
}
