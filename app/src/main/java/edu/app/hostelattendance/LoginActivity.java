package edu.app.hostelattendance;

import android.app.ProgressDialog;
import android.content.Intent;
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

    @BindView(R.id.rememberMe)
    CheckBox chk_rememberMe;

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
    private RequestParams requestParams = new RequestParams();
    private PersistentCookieStore persistentCookieStore ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        persistentCookieStore = new PersistentCookieStore(getApplicationContext());
        asyncHttpClient.setCookieStore(persistentCookieStore);
    }

    @OnClick(R.id.btn_sign_in)
    void signIn(View view){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"Logging In","Please wait...");
        progressDialog.show();

        persistentCookieStore.clear();

        requestParams.put("username", txt_registerNo.getText());
        requestParams.put("password",txt_password.getText());

        /*Check if valid*/
        asyncHttpClient.get("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do?method=isValidUser", requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {


                /*Attempt Login*/

                LoginActivity.this.asyncHttpClient.post("https://kp.christuniversity.in/KnowledgePro/StudentLoginAction.do?method=studentLoginAction", requestParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Document doc = Jsoup.parse(new String(responseBody));
                        Element name = doc.select(".name").first();

                        if(name != null){
                            Toast.makeText(LoginActivity.this, name.text(),Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, CheckAttendanceActivity.class));
                        }

                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials",Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                Toast.makeText(LoginActivity.this,"Internet not Connected",Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });



    }
}

