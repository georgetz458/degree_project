package com.example.weatheranalysis.classes.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.UploadUtils;
import com.example.weatheranalysis.classes.backgroundProcess.service.UploadService;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.example.weatheranalysis.classes.singleton.MySingletonVolley;
import com.example.weatheranalysis.classes.singleton.MyWorkManager;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
//τάξη για τη συλογή δεδομένων αισθητήρων
public class WeatherSensorRepository {

    private final Context context;
    private final SensorManager sensorManager;
    private final UploadService uploadService;
    private final String preferences_name, loginToken;
    private Sensor pressureSensor, lightSensor, proximitySensor;
    public SensorEventListener  sensorEventListenerPressure, sensorEventListenerLight, sensorEventListenerProximity;
    private JSONObject owResponse;
    private double proximity, light, phonePressure, batteryTemp;


    public WeatherSensorRepository(Context context, SensorManager sensorManager, UploadService uploadService, String preferences_name, String loginToken) {
        this.context = context;
        this.sensorManager = sensorManager;

        this.uploadService = uploadService;
        this.preferences_name = preferences_name;
        this.loginToken = loginToken;

    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    //αποκτά τη πίεση μέσω βαρόμετρου
    private void getPhonePressure() {

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (pressureSensor != null) {
            sensorEventListenerPressure = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {


                    //σε πείπτωση που δεν υπάρχει αλλαγή την επόμενη φορά


                    phonePressure = sensorEvent.values[0];

                    sensorManager.unregisterListener(this, pressureSensor);
                    sendData();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
            sensorManager.registerListener(sensorEventListenerPressure, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            //Log.d("Important", "This phone doesn't have a pressure sensor!");
            phonePressure = 0;
            sendData();



        }

    }

    //αποθήκευση/ενημέρωση χρονικής στιγμής της συλλογής
    private void storeCollectionLog(){
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        int num_collections = sharedPreferences.getInt("num_collections", 0);
        sharedPreferences.edit()
                .putLong("last_time_collecting",System.currentTimeMillis())
                .putInt("num_collections", num_collections + 1)
                .apply();
    }



    //αποκτά το φως μέσω αισθητήρα φωτός
        private void getPhoneLight() {

        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {

            sensorEventListenerLight = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {

                    light = sensorEvent.values[0];

                    sensorManager.unregisterListener(this, lightSensor);
                    getPhonePressure();
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
            sensorManager.registerListener(sensorEventListenerLight, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);


        }


        }
    //αποκτά proximity μέσω αισθητήρα proximity
    private void getPhoneProximity() {
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor != null) {
            sensorEventListenerProximity = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {

                    proximity = sensorEvent.values[0];
                    sensorManager.unregisterListener(this, proximitySensor);
                    getPhoneLight();

                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };
            sensorManager.registerListener(sensorEventListenerProximity, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }
    public void getSensorDataAndSendData(JSONObject owResponse, double batteryTemp){
        this.owResponse = owResponse;
        this.batteryTemp = batteryTemp;
        getPhoneProximity();
    }
    //αποστολή δεδομένων στο backend
    private void sendData(){
        boolean inPocket = uploadService.getWeatherUtils().isInPocket(proximity);
        double phoneTemp = uploadService.getWeatherUtils().getPhoneTempEstimation(inPocket, batteryTemp);
        JSONObject weatherData = new JSONObject();
        try{
            weatherData.put("openWeatherData", owResponse);
            weatherData.put("proximity", proximity);
            weatherData.put("light", light);
            weatherData.put("phonePressure", phonePressure);
            weatherData.put("phoneTemp", phoneTemp);
            weatherData.put("inPocket", inPocket);
            weatherData.put("timestamp", System.currentTimeMillis());
            weatherData.put("deviceManufacturer", Build.MANUFACTURER);
        } catch (JSONException e) {
            MySingletonNotificationManager.getInstance(context)
                    .setNotification(context.getString(R.string.error), context.getString(R.string.work_failure));
            return;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer "+loginToken);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, UploadUtils.CollectURL, weatherData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        double onlineTemp = 0;
                        try {
                            onlineTemp = uploadService.getWeatherUtils().getOnlineTemp(owResponse);
                        } catch (JSONException e) {//επαναπροσπάθεια από την αρχή δρομολογώντας work
                            MyWorkManager.getInstance(context)
                                    .retryCollectWork();
                            uploadService.onDestroy();
                        }
                        boolean isOutdoors = uploadService.getWeatherUtils().isOutdoors(onlineTemp, phoneTemp, light);
                        storeCollectionLog();
                        uploadService.initNewWork(isOutdoors, phoneTemp, onlineTemp, light, preferences_name);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse == null ){
                            MyWorkManager.getInstance(context)
                                            .retryCollectWork();
                            uploadService.onDestroy();

                        } else if (error.networkResponse.statusCode == 401) {

                        MySingletonNotificationManager.getInstance(context)
                                .setNotification(context.getString(R.string.token_expired),
                                        context.getString(R.string.token_expired_notification_message));
                        FirebaseAuth.getInstance().signOut();
                        uploadService.onDestroy();


                    } else {

                            MyWorkManager.getInstance(context)
                                    .retryCollectWork();
                            uploadService.onDestroy();
                        }
                    }


                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        MySingletonVolley.getInstance(context).getRequestQueue().add(request);


    }

}
