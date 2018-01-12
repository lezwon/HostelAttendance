package edu.app.hostelattendance;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;

public class AlarmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static final int MINUTE_DELAY = 1;
    // Create the Handler object (on the main thread by default)
    private Handler handler = new Handler();
    private Context context;
    private Date timeout;
    private SharedPreferences sharedPreferences;
    private String status;


    @Override
    public void onReceive(Context context, Intent intent) {

        Intent showActivityIntent = new Intent(context, CheckAttendanceActivity.class);
        showActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(showActivityIntent);

//        this.context = context;
////        StaticWakeLock.lockOn(context);
//        Calendar morning = Calendar.getInstance();
//        morning.set(Calendar.HOUR_OF_DAY, 8);
//        morning.set(Calendar.MINUTE, 45);
//        morning.set(Calendar.SECOND, 0);
//
//        Calendar evening = Calendar.getInstance();
//        evening.set(Calendar.HOUR_OF_DAY, 21);
//        evening.set(Calendar.MINUTE, 30);
//        evening.set(Calendar.SECOND, 0);
//
//        sharedPreferences = context.getSharedPreferences("HostelAttendance", Context.MODE_PRIVATE);
//        sharedPreferences.edit().remove("signInStatus").apply();
//        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
//
//        Date currentDate = new Date();
//
//        if (currentDate.before(morning.getTime()))
//            timeout = morning.getTime();
//        else
//            timeout = evening.getTime();
//
//        handler.post(runnableCode);
    }

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
//            startWakefulService(context,new Intent(context,CheckSignInStatusService.class));
            context.startService(new Intent(context,CheckSignInStatusService.class));
            handler.postDelayed(runnableCode, 1000 * 60 * MINUTE_DELAY);
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Intent signInAlarmIntent = new Intent("edu.app.hostelattendance.sign_in_notification");
            Toast.makeText(context,"signed in",Toast.LENGTH_SHORT).show();
            AlarmBroadcastReceiver.this.status = prefs.getString("signInStatus",null);

            if(status == null || (status.equals("Absent") && new Date().before(timeout))) { //pending
//                handler.postDelayed(runnableCode, 1000 * 60 * MINUTE_DELAY);
                signInAlarmIntent.putExtra("data", status);
            }
            else {
                handler.removeCallbacks(runnableCode);
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
                if(status.equals("Absent") && new Date().after(timeout)) {
                                /*Missed*/
                    signInAlarmIntent.putExtra("data", "Missed");
                    context.sendBroadcast(signInAlarmIntent);
                    Toast.makeText(context,"Missed",Toast.LENGTH_LONG).show();
                }
                else
                {
                    /*Leave or Signed in*/
                    signInAlarmIntent.putExtra("data", status);
                    StaticWakeLock.lockOff(context);
                }
            }
            context.sendBroadcast(signInAlarmIntent);
        }
    };
}
