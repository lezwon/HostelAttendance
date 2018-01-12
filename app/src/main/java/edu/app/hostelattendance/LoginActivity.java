package edu.app.hostelattendance;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity{


    @BindView(R.id.registerNo)
    TextView txt_registerNo;

    @BindView(R.id.password)
    TextView txt_password;


    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
//    private SyncHttpClient asyncHttpClient = new SyncHttpClient();
    private RequestParams requestParams = new RequestParams();
    private PersistentCookieStore persistentCookieStore ;
    private ProgressDialog progressDialog;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        persistentCookieStore = new PersistentCookieStore(getApplicationContext());
//        asyncHttpClient.setCookieStore(persistentCookieStore);
        asyncHttpClient.setCookieStore(persistentCookieStore);

        progressDialog = ProgressDialog.show(this,"Please wait...","Initializing Login Page");
        progressDialog.show();
        sharedPreferences = getSharedPreferences("HostelAttendance",MODE_PRIVATE);
        checkUserLoggedIn();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String name = sharedPreferences.getString("name",null);

        if (name != null)
            startActivity(new Intent(this, CheckAttendanceActivity.class));
    }

    private void checkUserLoggedIn() {

        asyncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do?method=returnHomePage", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Document doc = Jsoup.parse(new String(responseBody));
                Element name = doc.select(".name").first();

                progressDialog.dismiss();

                if(name != null) {
                    startActivity(new Intent(LoginActivity.this, CheckAttendanceActivity.class));
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

    }

    @OnClick(R.id.btn_sign_in)
    void signIn(View view){
        progressDialog = ProgressDialog.show(this,"Logging In","Please wait...");

        progressDialog.show();

        persistentCookieStore.clear();

        requestParams.put("username", txt_registerNo.getText());
        requestParams.put("password",txt_password.getText());
        requestParams.put("generatedCaptchaHash","673dbeb6bba34a75584404488455d987");
        requestParams.put("generatedCaptcha","8e2K6e/4CTA=");
        requestParams.put("enteredCaptcha","089d52");

        /*Check if valid*/
        asyncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do?method=isValidUser", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                LoginActivity.this.asyncHttpClient.post("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do?method=studentLoginAction", LoginActivity.this.requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Document doc = Jsoup.parse(new String(responseBody));
                        Element name = doc.select(".name").first();

                        if(name != null){

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", name.text());
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, CheckAttendanceActivity.class);
                            startActivity(intent);
                        }
                        else
                            Toast.makeText(LoginActivity.this, "Incorrect Credentials", Toast.LENGTH_LONG).show();

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials",Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                });

                /*String page = new String(responseBody);
                Document doc = Jsoup.parse(page);
                Element image = doc.select("#captcha_img").first();
                String imageLink = image.attr("src");
                imageLink = "https://kp.christuniversity.in/KnowledgePro/".concat(imageLink);


                RequestParams requestParams = new RequestParams();
                requestParams.add("url",imageLink);
                AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
                asyncHttpClient.addHeader("apikey","b45d91153188957");
                asyncHttpClient.post("https://api.ocr.space/parse/image", requestParams, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            response.getJSONArray(0).getString(1);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                });*/

                /*Attempt Login*/


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                Toast.makeText(LoginActivity.this,"Internet not Connected",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });



    }
}

