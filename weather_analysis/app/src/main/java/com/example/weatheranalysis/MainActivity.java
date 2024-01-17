package com.example.weatheranalysis;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;
import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.example.weatheranalysis.classes.MySignedActivityTemplate;
import com.example.weatheranalysis.classes.util.GeneralUtils;
import com.example.weatheranalysis.classes.LoadingDialog;
import com.example.weatheranalysis.classes.util.UploadUtils;
import com.example.weatheranalysis.classes.backgroundProcess.ProcessCheckerReceiver;
import com.example.weatheranalysis.classes.backgroundProcess.service.ActivityRecognitionService;
import com.example.weatheranalysis.classes.backgroundProcess.service.UploadService;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.example.weatheranalysis.classes.singleton.MyWorkManager;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

//κύριο activity που ο χρήστης ξεκινά/σταματά τη καταγραφή
public class MainActivity extends MySignedActivityTemplate {


    static final int REQ_LOC_CODE = 1;
    static final int REQ_LOC_CODE_COARSE = 2;
    static final int REQ_LOC_CODE_BACKGROUND = 3;
    static final int REQ_POST_CODE = 4;
    static final int REQ_ACTIVITY_CODE = 5;

    private boolean  hasFineLocPerm,  hasGPS;
    private String preferences_name;
    private Button workButton;
    private SharedPreferences preferences;
    public static boolean Stop = false;
    private TextView numCollectingTextView, lastTimeCollectingTextView;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        preferences_name = GeneralUtils.getMainPreferencesName(getApplicationContext());
        workButton = findViewById(R.id.work_button);
        preferences = getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        numCollectingTextView = findViewById(R.id.num_collections_textView);
        lastTimeCollectingTextView = findViewById(R.id.last_time_collecting_textView);
        setMenu();


        //TODO remove when in production
        //for trusting the self signed certificate
        //UploadUtils.handleSSLHandshake();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(!UploadUtils.isSingedIn(mAuth)){
            SharedPreferences introPreferences = getSharedPreferences("intro", Context.MODE_PRIVATE);
            if(introPreferences.getBoolean("isFirstTime", true)){
                GeneralUtils.goToActivity(this, IntroActivity.class);

            }
            else{
                GeneralUtils.goToActivity(this, LoginActivity.class);
            }

        }else{

            checkAutoStart();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if(UploadUtils.isSingedIn(FirebaseAuth.getInstance())){
            checkPermissions();

            long lastTimeCollecting = preferences.getLong("last_time_collecting", 0);
            int numCollections = preferences.getInt("num_collections", 0);
            if(lastTimeCollecting == 0){

                numCollectingTextView.setText(R.string.num_collection_default_text);
                lastTimeCollectingTextView.setText(R.string.last_time_collecting_default_text);

            }
            else{
                Timestamp timestamp = new Timestamp(lastTimeCollecting);
                String timestampString = timestamp.toString();

                numCollectingTextView.setText(getString(R.string.num_collection_desription)+numCollections);
                String lastTimeCollectingString = getString(R.string.last_time_collecting_description, timestampString);
                lastTimeCollectingTextView.setText(lastTimeCollectingString);
            }
        }

    }

    @Override
    protected void onStart() {

        super.onStart();

    }



    //static list για intents για AutoStart
    public static List<Intent> POWER_MANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    );
    //μέθοδος ελέγχου εκτέλεσης intent για AutoStart
    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    //μέθοδος ελέγχου AutoStart
    private void checkAutoStart() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("ProtectedApps", Context.MODE_PRIVATE);
        boolean skipMessage = settings.getBoolean("skipProtectedAppCheck", false);
        if (!skipMessage) {
            final SharedPreferences.Editor editor = settings.edit();
            boolean foundCorrectIntent = false;
            for (Intent intent : POWER_MANAGER_INTENTS) {
                if (isCallable(getApplicationContext(), intent)) {
                    foundCorrectIntent = true;


                    new AlertDialog.Builder(this)
                            .setMessage(R.string.autostart_prompt)

                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {

                                        Toast.makeText(getApplicationContext(), R.string.no_miui_prompt, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(getApplicationContext()
                                            , R.string.autostart_cancel_prompt, Toast.LENGTH_SHORT).show();
                                    finish();

                                }
                            })
                            .setCancelable(false)

                            .show();
                    break;
                }
            }
            if (!foundCorrectIntent) {
                editor.putBoolean("skipProtectedAppCheck", true);
                editor.apply();
            }
        }


    }

    //κίνεζικα roms σκοτώνουν την εφαρμογή μετά απο κάποια λεπτά
    //με αποτέλεσμα να μη λειτουργεί ο job scheduler, και κατ' επέκταση ο worker.
    // οπότε πρέπει να απενεργοποιηθεί ο battery saver
    private void checkIgnoreBatteryOpt() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        String packageName = getPackageName();
        boolean ignoringBattery = pm.isIgnoringBatteryOptimizations(packageName);
        String manufacturer = Build.MANUFACTURER;
        boolean manufacturerOfKillingApps = manufacturer.equalsIgnoreCase("Xiaomi") ||
                manufacturer.equalsIgnoreCase("Oppo") ||
                manufacturer.equalsIgnoreCase("Vivo") ||
                manufacturer.equalsIgnoreCase("Huawei") ||
                manufacturer.equalsIgnoreCase("Asus");
        if (manufacturerOfKillingApps && !ignoringBattery) {
            //σε αντίθεση με το stock android(εαν χρειαζόταν να εκτελεστεί, που δεν εκτελείται), δεν εμφανίζεται prompt
            // οπότε δημιουργήται ένα alert dialog.
            new AlertDialog.Builder(this)
                    .setMessage(R.string.request_ignore_batttery_prompt)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();

        }


        else if(activityManager.isBackgroundRestricted() ){
            new AlertDialog.Builder(this)
                    .setMessage(R.string.battery_restriction_lift_prompt)
                    .show();


        }else{//όλα βαίνουν καλώς
            startWork();
        }
        //για stock android προεπιλεγμένα μπαίνει σε dozer και εκτελείται στα maintenance windows
    }

    //τρέχει σε κάθε resume, ένα resume είναι και όταν από ένα prompt επιστρέφει στο activity
    //οι άδειες είναι για location, ActivityRecognition, exact alarm
    private void checkPermissions() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            //εάν δόθηκε fine location access
        ) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //εάν δόθηκαν όλες οι απαραίτητες άδεις location
                //ζητούνται ActivityRecognition, exact alarm
                hasFineLocPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQ_ACTIVITY_CODE);
                }else{
                    if(!alarmManager.canScheduleExactAlarms()){
                        startActivity(new Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
                    }
                }

            }
            //prompt για background location
            else {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.background_location_request)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestBackgroundLocation();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), getText(R.string.no_permissions_message), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        })
                        .setCancelable(false).show();
            }

        } else if( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
         && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){


            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQ_LOC_CODE_COARSE);
        }
        else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //εάν δε δόθηκε fine access τότε ζητήται η εναλλαγή σε αυτή


            new AlertDialog.Builder(this)
                    .setMessage(R.string.fine_location_prompt)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{

                                    Manifest.permission.ACCESS_FINE_LOCATION
                            }, REQ_LOC_CODE);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setCancelable(false).show();

        }
    }

    //ζητά background location permission
    private void requestBackgroundLocation() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQ_LOC_CODE_BACKGROUND);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_LOC_CODE:
            case REQ_LOC_CODE_BACKGROUND:
            case REQ_LOC_CODE_COARSE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.no_permissions_message, Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            case REQ_ACTIVITY_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.no_activity_recognition_request, Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            case REQ_POST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.no_notification_request, Toast.LENGTH_SHORT).show();
                    finish();
                }




        }


    }

    //εαν η τοποθεσία είναι απενεργοποιημένη εμφανίζει Prompt ενεργοποίησής της
    private void requestEnablingLocation() {
        LocationRequest locationRequest;
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 0).build();
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true).build();
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest);

        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {

                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        // προβολή του διαλόγου καλώντας την startResolutionForResult()
                        resolvable.startResolutionForResult(
                                MainActivity.this,
                                Priority.PRIORITY_BALANCED_POWER_ACCURACY);



                    } catch (IntentSender.SendIntentException exception) {
                        /// Αγνοήται το σφάλμα

                    }
                }
            }
        });

        result.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                checkIgnoreBatteryOpt();
            }
        });


    }
    //μέθοδος εκκίνησης καταγραφής με το πάτημα κουμπιού
    public void startCollection(View view){

        if(hasFineLocPerm && !hasGPS){
            requestEnablingLocation();
        }
        else{
            //εαν δεν χρειάζεται battery opt ή έχει ήδη αγνοηθεί τότε ξεκινά το work
            checkIgnoreBatteryOpt();
        }

    }


    //μέθοδος αρχικοποίησης καταστάσεων για την εκκίνηση καταγραφής
    private void startWork() {
        Stop = false;
        SharedPreferences.Editor editor = preferences.edit();
        stopPotentialExistingWork(editor);


            if (Build.VERSION.SDK_INT >= 33) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_CODE);
                }else{

                    checkLocationAndNetworkAccess(editor);
                }
            }else{
                checkLocationAndNetworkAccess(editor);

            }
    }
    //έλεγχος δικτύου και απόκτησης τρέχουσας τοποθεσίας
    private void checkLocationAndNetworkAccess(SharedPreferences.Editor editor) {

        LoadingDialog loadingDialog = new LoadingDialog(this);
        loadingDialog.loadDialog();
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if(networkCapabilities == null){
            loadingDialog.removeDialog();
            GeneralUtils.showMessage(MainActivity.this, getString(R.string.error), getString(R.string.no_network));
            return;


        }
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

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

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {




            String tag_name = getResources().getString(R.string.worker_tag_name);
            WorkManager.getInstance(this).cancelAllWorkByTag(tag_name);


            MySingletonNotificationManager.getInstance(this).setNotification(getString(R.string.location_perm_altered_title),
                    getString(R.string.location_perm_altered_content));
            loadingDialog.removeDialog();
            GeneralUtils.showMessage(MainActivity.this, getString(R.string.no_location), getString(R.string.try_again));
           return;


        }
        fusedLocationProviderClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            loadingDialog.removeDialog();
                            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            setWork(editor);

                        }
                        else{
                            loadingDialog.removeDialog();
                            GeneralUtils.showMessage(MainActivity.this, getString(R.string.error), getString(R.string.work_failure));

                        }
                    }
                });
    }
    //δρομολόγηση της καταγραφής
    private void setWork(SharedPreferences.Editor editor){

        editor.putBoolean(GeneralUtils.WORK_PREFERENCE_NAME, true);
        editor.apply();

        MyWorkManager myWorkManager = MyWorkManager.getInstance(getApplicationContext());
        myWorkManager.scheduleCollectWork(1);

        getSharedPreferences(getString(R.string.preferences_name), MODE_PRIVATE).edit().remove("StopPressed").apply();

        ProcessCheckerReceiver.setRepeatingAlarmForProcessCheck(getApplicationContext());
        Toast.makeText(this, R.string.start_work, Toast.LENGTH_SHORT).show();
        finish();
    }
    //παύση πιθανών διεργασίων
    private void stopPotentialExistingWork(SharedPreferences.Editor editor){
        //αφαίρεση WORK_PREFERENCE_NAME από το preferences
        editor.remove(GeneralUtils.WORK_PREFERENCE_NAME);
        editor.apply();
        workButton.setText(getText(R.string.start_work));

        //stop worker
        WorkManager myWorkManager = WorkManager.getInstance(getApplicationContext());
        myWorkManager.cancelAllWorkByTag(getString(R.string.worker_tag_name));
        cancelActivityAPIService();
        //stop Sensor Service

        Intent alarmIntent = new Intent(getApplicationContext(), UploadService.class);
        PendingIntent alarmPendingIntent = PendingIntent.getForegroundService(getApplicationContext(), 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(alarmPendingIntent);
        stopService(alarmIntent);
        //stop alarm check
        ProcessCheckerReceiver.cancelAlarmForProcessCheck(getApplicationContext());
        Stop = true;

        getSharedPreferences(getString(R.string.preferences_name), MODE_PRIVATE).edit()
                .putBoolean("StopPressed", true).apply();
    }
    public void stopWork(View view){
        SharedPreferences.Editor editor = preferences.edit();
        stopPotentialExistingWork(editor);


    }


    //εμφάνιση οδηγιών
    public void help(View view){
        if(!preferences.getBoolean("nextHelpPrompt", false)){
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.general_instructions_prompt_part_1))
                            .setPositiveButton(R.string.next, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    preferences.edit()
                                            .putBoolean("nextHelpPrompt", true)
                                            .apply();
                                    help(view);
                                }
                            })
                                    .show();


        }else {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.battery_saver_instructions_prompt_part_2))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            preferences.edit()
                                    .remove("nextHelpPrompt")
                                    .apply();

                        }
                    })
                    .setCancelable(false)
                    .show();
        }

    }

    @Override
    protected boolean homeButtonAction() {
        closeDrawer();
        return false;
    }

    @Override
    protected boolean infoButtonAction() {
        startInfoActivity();
        return false;
    }

    @Override
    protected boolean languageButtonAction() {
        startLanguageActivity();
        return false;
    }
    //παύση ActivityRecognition API
    private void cancelActivityAPIService() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }
        Intent intent = new Intent(this, ActivityRecognitionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE );

        Task<Void> task =  ActivityRecognition.getClient(this).removeActivityUpdates(pendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {


            }
        });

    }


}