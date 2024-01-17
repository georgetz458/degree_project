package com.example.weatheranalysis.classes.backgroundProcess;

import static android.content.Context.ALARM_SERVICE;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.toolbox.RequestFuture;
import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.GeneralUtils;
import com.example.weatheranalysis.classes.util.WeatherUtils;
import com.example.weatheranalysis.classes.repository.WeatherOWRepository;
import com.example.weatheranalysis.classes.backgroundProcess.service.ActivityRecognitionService;
import com.example.weatheranalysis.classes.backgroundProcess.service.UploadService;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.GetTokenResult;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
//worker για την απόκτηση θερμοκρασίας μπαταρίας και δεδομένων απο OpenWeather API
public class UploadWorker extends Worker {

    private final Context context;
    private double batteryTemp;
    private Location currentLocation;
    private SensorManager sensorManager;
    public final String openWeatherString = "https://api.openweathermap.org/data/2.5/weather";
    private Gson gson;
    private Intent intent;
    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;


    private FusedLocationProviderClient fusedLocationProviderClient;
    private RequestFuture<JSONObject> future;
    private String loginToken, preferences_name;
    private SharedPreferences sharedPreferences;
    private WeatherOWRepository repository;
    private WeatherUtils weatherUtils;
    private FirebaseAuth mAuth;
    private CompletableFuture<String> onGettingToken;

    //αρχικοποιεί μεταβλητές
    private void init() {

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        gson = new Gson();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        future = RequestFuture.newFuture();
        preferences_name = GeneralUtils.getMainPreferencesName(context);
        sharedPreferences = getApplicationContext().getSharedPreferences(preferences_name, Context.MODE_PRIVATE);

        repository = new WeatherOWRepository(getApplicationContext(), sensorManager, fusedLocationProviderClient, this, preferences_name);
        weatherUtils = new WeatherUtils(getApplicationContext(), preferences_name);
        mAuth = FirebaseAuth.getInstance();
        onGettingToken = new CompletableFuture<>();

    }

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;

    }

    //εκτελεί work
    @NonNull
    @Override
    public Result doWork() {

        init();
        if(sharedPreferences.getBoolean("StopPressed", false)){//εαν πρέπει να τερματιστεί

            return Result.failure();

        }
        else{
            SharedPreferences.Editor actRecPreferencesEditor = getApplicationContext().getSharedPreferences("ActRec", Context.MODE_PRIVATE).edit();
            actRecPreferencesEditor.remove("ActRec").apply();
            sharedPreferences.edit().putBoolean("isRunning", true).apply();
            readToken();
            try {
                loginToken = onGettingToken.get();
                repository.getBatteryTemperature();

                repository.getPhoneLocation();

            } catch (Exception e) {//εαν κάτι απρόοπτο και σταματίσει η διεργασία
                if(e instanceof ExecutionException | e instanceof InterruptedException){

                    return Result.retry();
                }
                else if(e instanceof FirebaseAuthInvalidUserException){//εαν δεν είναι έγκυρος χρήστης ακυρώνεται η διεργασία και αποσυνδέεται ο χρήστης
                    MySingletonNotificationManager.getInstance(context)
                            .setNotification(context.getString(R.string.error),context.getString(R.string.token_expired_notification_message));

                    mAuth.signOut();
                    return Result.failure();
                }
                else{//εαν αποτύχει η απόκτηση token επισρέφοντας completeExceptionally ή άλλαξε η άδεια της τοποθεσίας

                    return Result.failure();
                }
            }
            return sendData();
        }
    }

    @Override
    public void onStopped() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        Intent actRecIntent = null;
        PendingIntent actRecPendingIntent = null;
        if(sharedPreferences !=null){
             if(sharedPreferences.getBoolean("isActRecIntent", false)){
                 actRecIntent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
                 sharedPreferences.edit().remove("isActRecIntent").apply();

                 if(sharedPreferences.getBoolean("isActRecPendingIntent", false)){
                     actRecPendingIntent = PendingIntent.getService(getApplicationContext(), 0, actRecIntent, PendingIntent.FLAG_MUTABLE);

                     sharedPreferences.edit()
                             .remove("isActRecPendingIntent").apply();
                 }
             }
            sharedPreferences.edit().remove("isRunning").apply();
        }
        if(actRecPendingIntent != null){
            ActivityRecognition.getClient(getApplicationContext()).removeActivityUpdates(actRecPendingIntent);
            getApplicationContext().stopService(actRecIntent);
        }
        if(pendingIntent !=null){
            alarmManager.cancel(pendingIntent);
            getApplicationContext().stopService(intent);

        }

        super.onStopped();
    }

    //στέλνει δεδομένα στο UploadService μέσω της setAlarmSettings
    private Result sendData() {



        try {
            JSONObject response = future.get();


            JSONObject openWeatherResponse = new JSONObject(gson.toJson(response));




            setAlarmSettings(openWeatherResponse, batteryTemp, loginToken);
            setUploadAlarm();
            return Result.success();
        } catch (ExecutionException | InterruptedException | JSONException e) {

            return Result.retry();
        }

    }



    //ρύθμιση του Alarm Manager και PendingIntent
    private void setAlarmSettings(JSONObject owResponse, double batteryTemp, String loginToken){
        intent = new Intent(getApplicationContext(), UploadService.class);
        intent.putExtra("owResponse", owResponse.toString());
        intent.putExtra("batteryTemp", batteryTemp);
        intent.putExtra("loginToken", loginToken);
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    }
    //άμεση δρομολόγηση εκκίνησης του UploadService
   private void setUploadAlarm(){
        if(sharedPreferences.getBoolean("isRunning", false)){
            sharedPreferences.edit().remove("isRunning").apply();


            if (alarmManager.canScheduleExactAlarms()){
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP
                        , SystemClock.elapsedRealtime(),  pendingIntent);
            }else{
                MySingletonNotificationManager.getInstance(context)
                        .setNotification(context.getString(R.string.cannot_alarm_notif_title), context.getString(R.string.cannot_alarm_notif_message));


            }

        }

   }
    //ανάγνωση του token από τη firebase
    private void readToken(){

        if(FirebaseAuth.getInstance().getCurrentUser() !=null){
            FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult getTokenResult) {


                    onGettingToken.complete(getTokenResult.getToken());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    onGettingToken.completeExceptionally(e);
                }
            });
        }else{

            mAuth.signOut();
            MySingletonNotificationManager.getInstance(context)
                    .setNotification(context.getString(R.string.error),context.getString(R.string.token_expired_notification_message));
            onGettingToken.completeExceptionally(new Exception());
        }


    }

    public void removeWork(){

        sharedPreferences.edit()
            .remove(GeneralUtils.WORK_PREFERENCE_NAME)
        .apply();
    }

    //getters, setters

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public RequestFuture<JSONObject> getFuture() {
        return future;
    }


    public void setBatteryTemp(double batteryTemp) {
        this.batteryTemp = batteryTemp;
    }

}
