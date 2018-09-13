package edu.app.hostelattendance;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Date;

public class CheckSignInStatusService extends Service {
    private AsyncHttpClient syncHttpClient;


    @Override
    public void onCreate() {
        super.onCreate();
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(this);
        syncHttpClient = new AsyncHttpClient();
        syncHttpClient.setCookieStore(persistentCookieStore);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkStatus(){
        RequestParams requestParams = new RequestParams("method", "getHostelStudentsAttendanceSummary");

        syncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do", requestParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(CheckSignInStatusService.this,responseString,Toast.LENGTH_SHORT).show();
                try {
                    throw throwable;
                } catch (Throwable throwable1) {
                    throwable1.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Document doc = Jsoup.parse(responseString);

                try {
                Element table = doc.select("table").last();
                Element tableRow = table.select("tr").last();
                Element data = tableRow.select("td").last();
                SharedPreferences sharedPreferences = getSharedPreferences("HostelAttendance",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

//                intent.putExtra("interval","evening");
                editor.putString("interval", "evening");
                editor.putString("currentTime", new Date().toString());

                if(!data.hasText()) {
                    data = tableRow.select("td").get(2);
                    editor.putString("interval", "morning");
                }

                editor.putString("signInStatus", data.text());
                editor.apply();

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Your hostel details are not available", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }

            }

        });
    }


//    @Override
    protected void onHandleIntent(Intent intent) {
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(this);
        syncHttpClient = new SyncHttpClient();
        syncHttpClient.setCookieStore(persistentCookieStore);

        checkStatus();
//        AlarmBroadcastReceiver.completeWakefulIntent(intent);
//        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



//        AlarmBroadcastReceiver.completeWakefulIntent(intent);

        checkStatus();
        return START_NOT_STICKY;
    }
}
