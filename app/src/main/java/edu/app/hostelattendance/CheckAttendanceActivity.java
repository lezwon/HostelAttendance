package edu.app.hostelattendance;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import cz.msebera.android.httpclient.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.Date;


public class CheckAttendanceActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{


    private ProgressBar progressBar;

    @BindView(R.id.signInText)
    TextView textViewStatus;

    @BindView(R.id.name)
    TextView txt_name;

    private AlarmManager alarmManager;
    private PendingIntent morningAlarm;
    private PendingIntent eveningAlarm;
    private Calendar morning;
    private Calendar evening;
    private SharedPreferences sharedPreferences;
    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    private ProgressDialog progressDialog;
    private boolean paused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(getApplicationContext());
//        asyncHttpClient.setCookieStore(persistentCookieStore);
        asyncHttpClient.setCookieStore(persistentCookieStore);

        progressDialog = ProgressDialog.show(this, "CU Hostel", "Please Wait...");
        progressDialog.show();
        checkUserLoggedIn();


        setContentView(R.layout.activity_check_attendance);
        ButterKnife.bind(this);
        progressBar = (ProgressBar) findViewById(R.id.indeterminateBar);

//        LocalBroadcastManager.getInstance(this).registerReceiver(signInHandler, new IntentFilter("check_sign_in"));
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setIndeterminate(true);
        sharedPreferences = getSharedPreferences("HostelAttendance", MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        String name = sharedPreferences.getString("name", null);
        txt_name.setText(name);

    }

    @OnClick(R.id.btn_clear_cookies)
    public void clearCookies(){
        new PersistentCookieStore(this).clear();
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
//        startActivity(new Intent(CheckAttendanceActivity.this, LoginActivity.class));
    }

    private void checkUserLoggedIn() {

        asyncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginNewAction.do?method=returnHomePage", null , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                Element name = doc.select(".name").first();
                progressDialog.dismiss();

                if(name == null) {
//                    sharedPreferences.edit().clear().apply();
                    startActivity(new Intent(CheckAttendanceActivity.this, LoginActivity.class));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        paused = true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!paused)
            return;

        String name = sharedPreferences.getString("name",null);
        if (name == null)
            startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void initializeAlarms(){
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        morning = Calendar.getInstance();
        morning.set(Calendar.HOUR_OF_DAY, 8);
        morning.set(Calendar.MINUTE, 0);
        morning.set(Calendar.SECOND, 0);
//        morning.add(Calendar.MINUTE,2);

        evening = Calendar.getInstance();
//        evening.set(Calendar.HOUR_OF_DAY, 21);
//        evening.set(Calendar.MINUTE, 0);
//        evening.set(Calendar.SECOND, 0);
        evening.add(Calendar.SECOND,15);


        // Create two different PendingIntents, they MUST have different requestCodes
//        Intent intent = new Intent("edu.app.hostelattendance.ITS_TIME");
        Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
        intent.putExtra("data","Absent");
        morningAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        eveningAlarm = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);

    }

    @OnClick(R.id.btn_logOut)
    public void logOut(View view){
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(this);
        persistentCookieStore.clear();
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
    }

    @OnClick(R.id.btn_checkStatus)
    public void showStatus(View view) {
        progressBar.setVisibility(View.VISIBLE);
        startService(new Intent(getBaseContext(), CheckSignInStatusService.class));
    }

    @OnClick(R.id.btn_leave)
    public void applyForLeave(){
        startActivity(new Intent(this, LeaveActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(signInHandler);
    }


    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            progressBar.setVisibility(View.INVISIBLE);

            if(!s.equals("currentTime"))
                return;

            // Get extra data included in the Intent
            String data = sharedPreferences.getString("signInStatus",null);
            String interval = sharedPreferences.getString("interval",null);
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

            alarmManager.set(AlarmManager.RTC_WAKEUP, morning.getTimeInMillis(), morningAlarm);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, morning.getTimeInMillis(), AlarmManager.INTERVAL_DAY,morningAlarm);
            alarmManager.set(AlarmManager.RTC_WAKEUP, evening.getTimeInMillis(), eveningAlarm);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, evening.getTimeInMillis(),AlarmManager.INTERVAL_DAY, eveningAlarm);
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
