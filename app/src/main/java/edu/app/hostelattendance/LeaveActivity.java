package edu.app.hostelattendance;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LeaveActivity extends AppCompatActivity {

    @BindView(R.id.spn_leave_type)
    Spinner spnLeaveType;

    @BindView(R.id.spn_leave_reason)
    Spinner spnLeaveReason;

    @BindView(R.id.btn_apply)
    Button btnApply;

    @BindView(R.id.editText_dateFrom)
    TextView dateFrom;

    @BindView(R.id.editText_dateTo)
    TextView dateTo;


    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private RequestParams requestParams = new RequestParams();
    private ProgressDialog progressDialog;
    PersistentCookieStore persistentCookieStore;
    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave);
        ButterKnife.bind(this);
        persistentCookieStore = new PersistentCookieStore(getApplicationContext());
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        asyncHttpClient.setCookieStore(persistentCookieStore);
        url = "https://kp.christuniversity.in/KnowledgePro/hostelLeave1.do?method=saveApplyLeave";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..");

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        Date saturday = c.getTime(); // => Date of this coming Saturday.
        c.add(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date sunday = c.getTime(); // => Date of this coming Sunday.

        dateFrom.setText(format.format(saturday));
        dateTo.setText(format.format(sunday));

        requestParams.add("method","saveApplyLeave");
        requestParams.add("formName","hostelLeaveForm");
        requestParams.add("pageType","4");
        requestParams.add("startDate", format.format(saturday));
        requestParams.add("endDate", format.format(sunday));
        requestParams.add("leaveFromSession","Evening");
        requestParams.add("leaveToSession","Morning");

        spnLeaveType.setSelection(6);
        spnLeaveReason.setSelection(1);
    }

    @OnClick(R.id.btn_apply)
    public void applyForLeave(View view){
        progressDialog.show();
        requestParams.add("leaveType",String.valueOf(spnLeaveType.getSelectedItemPosition()));
        requestParams.add("reasons",spnLeaveReason.getSelectedItem().toString());

        asyncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/hostelLeave1.do?method=initStudentHostelLeave", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                asyncHttpClient.post(url, requestParams, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        spnLeaveType.setClickable(false);
                        spnLeaveReason.setClickable(false);
                        btnApply.setEnabled(false);

                        if (responseBody == null)
                            return;

                        Document doc = Jsoup.parse(new String(responseBody));

                        try {

                        Element fontElement = doc.select("font[color=red]").first();
                        if(!fontElement.hasText())
                            fontElement = doc.select("font[color=green]").first();

                        String response = null;
                        if (fontElement != null)
                            response   = fontElement.text();
                            AlertDialog alertDialog = new AlertDialog.Builder(LeaveActivity.this)
                                    .setMessage(response)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            LeaveActivity.this.finish();
                                        }
                                    }).create();

                            alertDialog.show();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), "Your hostel details are not available", Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            e.printStackTrace();
                        }



                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(LeaveActivity.this,"Error has Occurred",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });


    }
}
