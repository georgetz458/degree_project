package com.example.weatheranalysis.classes.util;

import static android.content.Context.CONNECTIVITY_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import com.example.weatheranalysis.classes.singleton.MyWorkManager;
import com.google.android.gms.location.DetectedActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Random;
//περιέχει μεθόδους που χρησιμέυουν στηκαταγραφή καιρικών δεδομένων
public class WeatherUtils {
    private Context context;
    private String preferences_name;

    public WeatherUtils(Context context, String preferences_name) {
        this.context = context;
        this.preferences_name = preferences_name;
    }
    //εξαγωγή θερμοκρασίας από το OpenWeather API response
    public double getOnlineTemp(JSONObject jsonObject) throws JSONException {
        JSONObject owNameValuePairs = jsonObject.getJSONObject("nameValuePairs");
        JSONObject weatherMainJSON = owNameValuePairs.getJSONObject("main").getJSONObject("nameValuePairs");
        return (weatherMainJSON .getDouble("temp") - 273.15);


    }
    //έκτίμηση εάν η συσκευή έιναι σε εξωτερικό χώρο
    public boolean isOutdoors( double temp, double tempEstimation, double light){
        if(light >= 10000) return true;//ηλιοφάνεια
        if(isNetworkCellular()) return true;//δίκτυο κινητής τηλεφωνείας
        return Math.abs(temp - tempEstimation) < 5;// διαφορά μεταξύ εκτίμησης θερμοκρασίας και θερμοκρασίας μικρότερη των 5 βαθμών Κελσίου
    }
    //έκτίμηση εάν η συσκευή έιναι σε εξωτερικό χώρο με γνώμονα και τη δραστηριότητα που πάρθηκε μέσω ActivityRecognition API
    public boolean isOutdoors(double temp, double tempEstimation, double light, int activity){

        switch (activity){
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
            case DetectedActivity.ON_BICYCLE:
                return true;
            default:
                return isOutdoors(temp, tempEstimation, light);
        }


    }

    //εκτίμηση εάν η συσκευή είναι εκτός ή εντός τσέπης
    public boolean isInPocket( double proximity){//check if indoors/outdoors
        SharedPreferences sharedPreferences  = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(!sharedPreferences.contains("proximity")){
            if(proximity==0){
                editor.putFloat("proximity", (float) proximity);
                editor.putLong("firstTimeInPocket", System.currentTimeMillis());
                editor.apply();
            }
        }
        else{

            if(proximity == 0){
                long firstTimeInPocket = sharedPreferences.getLong("firstTimeInPocket", 0);
                long currentTime = System.currentTimeMillis();
                //εάν ξεπερνά τα 20 λεπτά τότε θεωρείται InPocket η θεμροκρασία
                double dt = ((double) (currentTime - firstTimeInPocket)/1000)/60;
                if(dt >= 20){
                    editor.putLong("timeOfPocketTemp", currentTime);
                }
                return dt >= 20;
            }
            else{//εάν περάσουν
                if((System.currentTimeMillis() - sharedPreferences.getLong("firstTimeInPocket", 0) > 2*60*1000)){
                    editor.remove("proximity");
                    editor.remove("firstTimeInPocket");
                }
                if(sharedPreferences.contains("timeOfPocketTemp")){
                    long firstTimeInPocket = sharedPreferences.getLong("timeOfPocketTemp", 0);
                    long currentTime = System.currentTimeMillis();

                    double dt = ((double) (currentTime - firstTimeInPocket)/1000)/60;
                    if(dt <20) return true;
                    else {
                        editor.remove("timeOfPocketTemp");
                        return false;
                    }
                }
                editor.apply();
            }

        }
        return false;
    }
    //υπολογισμός εκτίμησης θερμοκρασίας από τη θερμοκρασία μπαταρίας και το αν είναι σε τσέπη ή όχι
    public double getPhoneTempEstimation(boolean inPocket, double batteryTemp){
        if(inPocket){
            return batteryTemp*1.877 - 35.1;
        }
        else{
            return batteryTemp*0.903 +1.074;
        }
    }
    //εάν έιναι το δίκτυο κινητής τηλεφωνείας
    private boolean isNetworkCellular(){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if(networkCapabilities!=null){
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

        }else {
            return false;
        }
    }
    //δρομολόγηση νέου work
    public void enqueueUploadWork(boolean isOutdoors){
        int low, high, period;
        LocalDateTime localDateTime = LocalDateTime.now();
        //σε νυχτερινές ώρες δρομολογήται για το πρωί
         if (localDateTime.getHour() >= 22 || localDateTime.getHour() <= 6) {
            int lowWakeHour = 7;
            int highWakeHour = 8;
            int currentHour = localDateTime.getHour();
            int currentMin = localDateTime.getMinute();
            int dMin = 60 - currentMin;
            int timeToMidnight;
            if (currentHour >= 22) {
                timeToMidnight = 24 - currentHour;
            } else {
                timeToMidnight = -currentHour;
            }
            int dHL = lowWakeHour + timeToMidnight;
            int dHH = highWakeHour + timeToMidnight;
            low = dHL * 60 + dMin;
            high = dHH * 60 + dMin;
        }else if (isOutdoors) {//εάν ειναι σε εξωτερικό δρομολογήται μετά από 5 με 10 λεπτά
            low = 5;
            high = 10;
        } else {//εάν ειναι σε εσωτερικό δρομολογήται μετά από 10 με 20 λεπτά
            low = 10;
            high = 20;
        }
        Random random = new Random();
        period = low + random.nextInt(high - low);
        MyWorkManager myWorkManager = MyWorkManager.getInstance(context);
        myWorkManager.scheduleCollectWork(period);
    }

}
