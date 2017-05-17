package edu.app.hostelattendance;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.*;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import java.util.Calendar;
import java.util.Date;


public class CheckAttendanceActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {


    private ProgressDialog progressDialog;

    @BindView(R.id.signInText)
    TextView textViewStatus;

    @BindView(R.id.switch_alarm)
    Switch switchAlarm;

    private AlarmManager alarmManager;
    private PendingIntent morningAlarm;
    private PendingIntent eveningAlarm;
    private Calendar morning;
    private Calendar evening;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        ButterKnife.bind(this);
        SharedPreferences sharedPrefs = getSharedPreferences("HostelAttendance", MODE_PRIVATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(signInHandler, new IntentFilter("check_sign_in"));
        switchAlarm.setChecked(sharedPrefs.getBoolean("alarmStatus", false));
        switchAlarm.setOnCheckedChangeListener(this);

    }

    private void initializeAlarms(){
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        morning = Calendar.getInstance();
        morning.set(Calendar.HOUR_OF_DAY, 8);
        morning.set(Calendar.MINUTE, 0);
        morning.set(Calendar.SECOND, 0);
//        morning.add(Calendar.MINUTE,1);

        evening = Calendar.getInstance();
        evening.set(Calendar.HOUR_OF_DAY, 21);
        evening.set(Calendar.MINUTE, 0);
        evening.set(Calendar.SECOND, 0);
//        evening.add(Calendar.SECOND,20);


        // Create two different PendingIntents, they MUST have different requestCodes
        Intent intent = new Intent("edu.app.hostelattendance.ITS_TIME");
        intent.putExtra("data","Absent");
        morningAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
        eveningAlarm = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, 0);
    }


    @OnClick(R.id.btn_checkStatus)
    public void showStatus(View view) {
        progressDialog.show();
        startService(new Intent(getBaseContext(), CheckSignInStatusService.class));
    }

    @OnClick(R.id.btn_leave)
    public void applyForLeave(){
        startActivity(new Intent(this, LeaveActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(signInHandler);
    }

    private BroadcastReceiver signInHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String data = intent.getStringExtra("data");
            String interval = intent.getStringExtra("interval");
            Calendar cal = Calendar.getInstance();

            if(interval.equals("evening")){
                cal.set(Calendar.HOUR_OF_DAY,21);
                cal.set(Calendar.MINUTE,30);
            }
            else{

                if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                    textViewStatus.setText(R.string.sunday);
                    return;
                }

                cal.set(Calendar.HOUR_OF_DAY,8);
                cal.set(Calendar.MINUTE,45);
            }

            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            Date timeout = cal.getTime();

            if(data.equals("Absent") && new Date().before(timeout))
                textViewStatus.setText(R.string.pending);
            else if (data.equals("Absent") && new Date().after(timeout))
                textViewStatus.setText(R.string.absent);
            else if(data.equals("Leave Approved"))
                textViewStatus.setText(R.string.leave);
            else{
                String string = String.format(getResources().getString(R.string.signIn_success),data);
                textViewStatus.setText(string);
            }

            progressDialog.dismiss();
        }
    };

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        saveButtonStatus(b);
        initializeAlarms();
        setAlarm(b);
    }

    private void setAlarm(boolean b) {
        if(b){
            // Start both alarms, set to repeat once every day

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, morning.getTimeInMillis(), AlarmManager.INTERVAL_DAY, morningAlarm);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, evening.getTimeInMillis(), AlarmManager.INTERVAL_DAY, eveningAlarm);
            Toast.makeText(this,"Enabled Alarm",Toast.LENGTH_SHORT).show();
        }
        else{
            alarmManager.cancel(morningAlarm);
            alarmManager.cancel(eveningAlarm);
        }
    }

    private void saveButtonStatus(boolean b) {
        SharedPreferences.Editor editor = getSharedPreferences("HostelAttendance", MODE_PRIVATE).edit();
        editor.putBoolean("alarmStatus", b);
        editor.apply();
    }
}
