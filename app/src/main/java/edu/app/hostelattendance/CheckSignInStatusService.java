package edu.app.hostelattendance;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.Date;

public class CheckSignInStatusService extends IntentService {
    private PersistentCookieStore persistentCookieStore;
    private SyncHttpClient syncHttpClient;

    public CheckSignInStatusService() {
        super("CheckSignInStatusService");
    }

    private void checkStatus(){
        RequestParams requestParams = new RequestParams("method", "getHostelStudentsAttendanceSummary");

        syncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do", requestParams, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(CheckSignInStatusService.this,responseString,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Document doc = Jsoup.parse(responseString);
                Element table = doc.select("table").last();
                Intent intent = new Intent("check_sign_in");
                Intent signInAlarmIntent = new Intent("edu.app.hostelattendance.sign_in_notification");
                Element tableRow = table.select("tr").last();
                Element data = tableRow.select("td").last();

                intent.putExtra("interval","evening");

                if(!data.hasText()) {
                    data = tableRow.select("td").get(2);
                    intent.putExtra("interval","morning");
                }

                // You can also include some extra data.
                intent.putExtra("data", data.text());
                signInAlarmIntent.putExtra("data", data.text());

                SharedPreferences sharedPreferences = getSharedPreferences("HostelAttendance",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("signInStatus", data.text());
                editor.apply();

//
                CheckSignInStatusService.this.sendBroadcast(signInAlarmIntent);
            }

        });
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        persistentCookieStore = new PersistentCookieStore(this);
        syncHttpClient = new SyncHttpClient();
        syncHttpClient.setCookieStore(persistentCookieStore);

        checkStatus();
//        AlarmActiveBroadcastReceiver.completeWakefulIntent(intent);
        stopSelf();
    }
}
