package edu.app.hostelattendance;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.icu.lang.UScript;
import android.media.RingtoneManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;

import java.util.Date;

public class NotificationBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        String data = intent.getStringExtra("data");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000});

        Intent intentActivity = new Intent(context, CheckAttendanceActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CheckAttendanceActivity.class);
        stackBuilder.addNextIntent(intentActivity);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        switch (data) {
            case "Absent":
//            TODO: Call when person has'nt signed in yet
                mBuilder.setSmallIcon(R.drawable.ic_pending);
                mBuilder.setContentTitle("You have not signed in yet");
                mBuilder.setContentText("Do not forget to sign in");
                break;
            case "Leave Approved":
                mBuilder.setSmallIcon(R.drawable.ic_leave);
                mBuilder.setContentTitle("You are on Leave");
                mBuilder.setContentText("You do not have to sign in");
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                break;
            case "Missed":
                mBuilder.setSmallIcon(R.drawable.ic_missed);
                mBuilder.setContentTitle("You have missed sign in");
                mBuilder.setContentText("You did'nt sign in before timeout");
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                break;
            default:
                // TODO: Call when person has signed in
                mBuilder.setSmallIcon(R.drawable.ic_tick);
                mBuilder.setContentTitle("You have successfully signed in");
                mBuilder.setContentText("You have signed in at "+data);
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                break;
        }


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0,mBuilder.build());
    }
}
