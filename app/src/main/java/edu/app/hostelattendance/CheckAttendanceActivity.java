package edu.app.hostelattendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.Date;


public class CheckAttendanceActivity extends AppCompatActivity {

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private RequestParams requestParams = new RequestParams();
    private PersistentCookieStore persistentCookieStore;
    ProgressDialog progressDialog;


    @BindView(R.id.signInText)
    TextView textViewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);
        persistentCookieStore = new PersistentCookieStore(getApplicationContext());
        asyncHttpClient.setCookieStore(persistentCookieStore);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_checkStatus)
    public void showStatus(View view) {

        progressDialog.show();
        RequestParams requestParams = new RequestParams("method", "getHostelStudentsAttendanceSummary");
        asyncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Calendar cal = Calendar.getInstance();
                Document doc = Jsoup.parse(new String(responseBody));
                Element table = doc.select("table").last();
                Element tableRow = table.select("tr").last();
                Element data = tableRow.select("td").last();

                if(data.hasText()){
                    cal.set(Calendar.HOUR_OF_DAY,21);
                    cal.set(Calendar.MINUTE,30);
                }
                else{

                    if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                        textViewStatus.setText(R.string.sunday);
                        return;
                    }

                    data = tableRow.select("td").get(2);
                    cal.set(Calendar.HOUR_OF_DAY,8);
                    cal.set(Calendar.MINUTE,45);
                }

                cal.set(Calendar.SECOND,0);
                cal.set(Calendar.MILLISECOND,0);
                Date timeout = cal.getTime();

                if(data.text().equals("Absent") && new Date().before(timeout))
                    textViewStatus.setText(R.string.pending);
                else if (data.text().equals("Absent") && new Date().after(timeout))
                    textViewStatus.setText(R.string.absent);
                else if(data.text().equals("Leave Approved"))
                    textViewStatus.setText(R.string.leave);
                else{
                    String string = String.format(getResources().getString(R.string.signIn_success),data.text());
                    textViewStatus.setText(string);
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }

        });
    }

    @OnClick(R.id.btn_leave)
    public void applyForLeave(){
        startActivity(new Intent(this, LeaveActivity.class));
    }

}
