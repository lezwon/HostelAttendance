package edu.app.hostelattendance;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class NotificationBroadcastReceiver extends WakefulBroadcastReceiver {

    private Handler handler = new Handler();
    private static final int MINUTE_DELAY = 1;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        this.context = context;
        String data = intent.getStringExtra("data");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000, 1000})
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM);


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

        Calendar morning = Calendar.getInstance();
        morning.set(Calendar.HOUR_OF_DAY, 8);
        morning.set(Calendar.MINUTE, 45);
        morning.set(Calendar.SECOND, 0);

        Calendar evening = Calendar.getInstance();
        evening.set(Calendar.HOUR_OF_DAY, 21);
        evening.set(Calendar.MINUTE, 30);
        evening.set(Calendar.SECOND, 0);


//        Date currentDate = new Date();

//        Date timeout;
//        if (currentDate.before(morning.getTime()))
//            timeout = morning.getTime();
//        else
//            timeout = evening.getTime();

//        if(data == null || (data.equals("Absent") && new Date().before(timeout))){
//            //pending
//            handler.postDelayed(runnableCode, 1000 * 60 * MINUTE_DELAY);
////            return;
//        }
//        else {
//            if(data.equals("Absent") && new Date().after(timeout)) {
//                data = "Missed";
//            }
//
//        }


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

        if (!data.equals("Absent"))
            StaticWakeLock.lockOff(context);
    }

//    private Runnable runnableCode = new Runnable() {
//        @Override
//        public void run() {
////            startWakefulService(context,new Intent(context,CheckSignInStatusService.class));
//            context.startService(new Intent(context,CheckSignInStatusService.class));
//        }
//    };
}
