package com.example.weatheranalysis.classes.backgroundProcess;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.backgroundProcess.service.ActivityRecognitionService;
import com.example.weatheranalysis.classes.backgroundProcess.service.UploadService;
import com.example.weatheranalysis.classes.singleton.MySingletonNotificationManager;
import com.example.weatheranalysis.classes.singleton.MyWorkManager;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
//broadcast receiver για τον έλεγχο εκτέλεσης ή δρομολόγησης διεργασιών
public class ProcessCheckerReceiver extends BroadcastReceiver {
    private Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        checkProcess();
    }



    private void checkProcess(){
        //εάν δεν τρέχει ή δρομολογηθεί καποια διεργασία τότε δρομολογήται work
        if(!(isWorkerEnqueuedOrRunning() || isAlarmRunning() || isActRecRunning())){

            MyWorkManager myWorkManager = MyWorkManager.getInstance(context);
            myWorkManager.scheduleCollectWork(1);
            setRepeatingAlarmForProcessCheck(context);


        }
        else{


            setRepeatingAlarmForProcessCheck(context);
        }
    }
    private boolean isActRecRunning(){

        return ActivityRecognitionService.Active;
    }

    private boolean isAlarmRunning(){

        return UploadService.Active;
    }
    private boolean isWorkerEnqueuedOrRunning(){

        WorkManager instance = WorkManager.getInstance(context);
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosByTag(context.getString(R.string.worker_tag_name));
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;

            }
            return running;
        } catch (ExecutionException | InterruptedException e) {

            return false;
        }
    }
    //δρομολόγηση νέου έλεγχου
    //ανά 6 ώρες
    public static void setRepeatingAlarmForProcessCheck(Context context){
        Intent intent = new Intent(context, ProcessCheckerReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (alarmManager.canScheduleExactAlarms()){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 60*60*1000, pendingIntent);
        }
        else {
            MySingletonNotificationManager.getInstance(context)
                    .setNotification(context.getString(R.string.cannot_alarm_notif_title), context.getString(R.string.cannot_alarm_notif_message));
        }


    }
    //παύση ελέγχων διεργασιών
    public static void cancelAlarmForProcessCheck(Context context){
        //stop Process Check Service

        Intent processAlarmIntent = new Intent(context, ProcessCheckerReceiver.class);
        PendingIntent processAlarmPendingIntent = PendingIntent.getBroadcast(context, 0,
                processAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.cancel(processAlarmPendingIntent);
        context.stopService(processAlarmIntent);
    }
}
