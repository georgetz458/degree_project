package com.example.weatheranalysis.classes.singleton;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.backgroundProcess.UploadWorker;

import java.util.concurrent.TimeUnit;
//singleton για τη δρομολόγηση work
public class MyWorkManager {
    private static MyWorkManager instance;
    private Context context;
    private WorkManager workManager;

    private MyWorkManager( Context context) {
        this.context = context;
        workManager = WorkManager.getInstance(context);
    }
    public static synchronized MyWorkManager getInstance(Context context){
        if(instance == null){
            instance = new MyWorkManager(context);
        }
        return instance;
    }
    //το delay δουλέυει σαν δυναμική περίοδος. Κάθε νέο work έχει διαφορετικό delay
    public void scheduleCollectWork(int delay){
        Constraints constraints = new Constraints.Builder()
                .setRequiresCharging(false)
                .setRequiresBatteryNotLow(true)
                .build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadWorker.class)
                .setConstraints(constraints)
                .addTag(context.getString(R.string.worker_tag_name))
                .setInitialDelay(delay, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.MINUTES)
                .build();
        workManager.enqueueUniqueWork(context.getString(R.string.unique_work_name), ExistingWorkPolicy.REPLACE, workRequest);
    }
    public void cancelCollectWork(){
        workManager.cancelAllWorkByTag(context.getString(R.string.worker_tag_name));
    }
    //επαναπροσπάθεια για εκτέλεση work
    public void retryCollectWork(){
        cancelCollectWork();
        scheduleCollectWork(1);

    }
}
