package com.example.weatheranalysis.classes.repository;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.BatteryManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.UploadUtils;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.example.weatheranalysis.classes.singleton.MySingletonVolley;
import com.example.weatheranalysis.classes.backgroundProcess.UploadWorker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
//τάξη για τη συλλογή δεδομένων από τη μπαταρία και το OpenWeather API
public class WeatherOWRepository {

    private Context context;
    private SensorManager sensorManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private UploadWorker uploadWorker;
    private String preferences_name;


    public WeatherOWRepository(Context context, SensorManager sensorManager, FusedLocationProviderClient fusedLocationProviderClient, UploadWorker UploadWorker, String preferences_name) {
        this.context = context;
        this.sensorManager = sensorManager;
        this.fusedLocationProviderClient = fusedLocationProviderClient;

        this.uploadWorker = UploadWorker;
        this.preferences_name = preferences_name;
    }

    //αποκτά μέσω sticky intent τη θερμοκρρασία της μπαταρίας
    public void getBatteryTemperature() {

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = context.registerReceiver(null, intentFilter);
        uploadWorker.setBatteryTemp((double) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10);



    }





    //απόκτηση δεδομέων από api του OpenWeather
    //μέσω του  RequestFuture<JSONObject> future επιλέγεται να αποκτηθεί το response στη send data
    private void getOpenWeatherData(double latidute, double longitude) {

        String url = uploadWorker.openWeatherString + "?lat=" + latidute + "&lon=" + longitude + "&AppId=" + UploadUtils.AppId;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, uploadWorker.getFuture(), uploadWorker.getFuture());
        MySingletonVolley.getInstance(context).getRequestQueue().add(jsonObjectRequest);


    }

    //απόκτηση τοποθεσίας και εκτέλεση του getOpenWeatherData με τις συνταταγμένες
    public void getPhoneLocation() throws Exception {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
            }

            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
            }
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {



            uploadWorker.removeWork();
            String tag_name = context.getResources().getString(R.string.worker_tag_name);
            WorkManager.getInstance(context).cancelAllWorkByTag(tag_name);


            MySingletonNotificationManager.getInstance(context).setNotification(context.getString(R.string.location_perm_altered_title),
                    context.getString(R.string.location_perm_altered_content));
            throw new Exception();


        }
        fusedLocationProviderClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            uploadWorker.setCurrentLocation(task.getResult());
                            //Log.d("Important", "Location retrieved!");
                            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            getOpenWeatherData(uploadWorker.getCurrentLocation().getLatitude(), uploadWorker.getCurrentLocation().getLongitude());
                        }


                    }
                });
    }





}
