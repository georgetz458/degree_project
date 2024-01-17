package com.example.weatheranalysis.classes.singleton;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.WorkManager;

import com.example.weatheranalysis.MainActivity;
import com.example.weatheranalysis.R;
import com.example.weatheranalysis.classes.util.GeneralUtils;

import java.util.UUID;
//singleton για τις ειδοποιήσεις
public class MySingletonNotificationManager {

    private Context context;
    private NotificationManagerCompat notificationManager;
    private static MySingletonNotificationManager instance;

    private MySingletonNotificationManager(Context context) {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
    }
    public static synchronized MySingletonNotificationManager getInstance(Context context){
        if (instance == null){
            instance = new MySingletonNotificationManager(context);

        }
        return instance;
    }
    //δημιουργία ειδοποίησης
    public Notification setNotification(String title, String content) {//εάν δε δοθεί Permission τότε δεν εμφανίζεται.



            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);



        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(UUID.randomUUID());
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT  | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_baseline_notification_important_24)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .addAction(android.R.drawable.ic_delete,"cancel",intent)
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true);




        notificationManager.notify(GeneralUtils.NOTIFICATION_ID, builder.build());
        return builder.build();
    }

}
