package com.example.weibo_liweiquan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText phoneNumberEditText, verificationCodeEditText;
    private Button loginButton;
    private CountDownTimer countDownTimer;
    private int remainingSeconds = 60, flag = 0, flag2 = 0;
    private boolean isTimerRunning = false;
    private TextView backTextView, verificationTextView;
    private SharedPreferences prefs;
    private int clickCount;
    private String phone;
    private String smscode;
    private Handler handler = new Handler();
    private static final String TAG = "Login";
    private static final String Url = "https://hotfix-service-prod.g.mi.com";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        verificationCodeEditText = findViewById(R.id.verification_code_edit_text);
        loginButton = findViewById(R.id.login_button);
        backTextView = findViewById(R.id.backtext);
        verificationTextView = findViewById(R.id.verification_text);
        loginButton.setEnabled(false);
        prefs = getSharedPreferences("verificationCount", Context.MODE_PRIVATE);
        clickCount = prefs.getInt("clickCount", 0);

        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 11) {
                    flag = 1;
                    verificationTextView.setTextColor(Color.parseColor("#0D84FF"));
                    if(flag2 == 1){
                        loginButton.setEnabled(true);
                    }
                } else {
                    flag = 0;
                    verificationTextView.setTextColor(Color.parseColor("#CCCCCC"));
                    loginButton.setEnabled(false);
                }
            }
        });

        verificationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context LoginActivity = com.example.weibo_liweiquan.LoginActivity.this;
                if (!NetworkUtils.isNetworkConnected(LoginActivity)){
                    Toast.makeText(LoginActivity.this, "无网络！", Toast.LENGTH_SHORT).show();
                }
                else if (clickCount < 20) { //每天只能获取20次验证码
                    clickCount++;
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("clickCount", clickCount);
                    editor.apply();
                    if (!isTimerRunning) {
                        if(flag == 1){
                            phone = phoneNumberEditText.getText().toString();
                            getverification();
                            verificationTextView.setEnabled(false);
                            startCountDownTimer();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "请输入完整手机号！", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "每天只能获取20次！", Toast.LENGTH_SHORT).show();
                }

            }
        });

        verificationCodeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
//                if (s.length() == 6 && phoneNumberEditText.getText().toString().length() == 11) {
                if(s.length() == 6){
                    flag2 = 1;
                }
                else {
                    flag2 = 0;
                }
                if (s.length() == 6 && flag == 1) {
                    loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context LoginActivity = com.example.weibo_liweiquan.LoginActivity.this;
                if (!NetworkUtils.isNetworkConnected(LoginActivity)){
                    Toast.makeText(LoginActivity.this, "无网络！", Toast.LENGTH_SHORT).show();
                }
                else {
                    phone = phoneNumberEditText.getText().toString();
                    smscode = verificationCodeEditText.getText().toString();
                    logWithCode();//登录
                }
            }
        });
        backTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        checkAndUpdateDate();
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds--;
                verificationTextView.setTextColor(Color.parseColor("#CCCCCC"));
                verificationTextView.setText("获取验证码(" + remainingSeconds + "s)");
                isTimerRunning = true;
            }

            @Override
            public void onFinish() {
                verificationTextView.setEnabled(true);
                verificationTextView.setTextColor(Color.parseColor("#0D84FF"));
                verificationTextView.setText("获取验证码");
                remainingSeconds = 60;
                isTimerRunning = false;
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private void checkAndUpdateDate() {//通过日期判断

        String lastDate = prefs.getString("date", "");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        if (!currentDate.equals(lastDate)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("date", currentDate);
            editor.putInt("clickCount", 0);
            editor.apply();
            clickCount = 0;
        }
    }

    private void getverification() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("phone", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences1 = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        TokenManager tokenManager = new TokenManager(sharedPreferences1, this);
        // 初始化OkHttpClient并添加拦截器，当接口请求报403时删除token
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .build();


        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());

        Request request = new Request.Builder()
                .url("https://hotfix-service-prod.g.mi.com/weibo/api/auth/sendCode")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d(TAG, "Response: " + responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    int code = jsonResponse.getInt("code");
                    String msg = jsonResponse.getString("msg");
                    boolean data = jsonResponse.getBoolean("data");

                    Log.d(TAG, "Code: " + code);
                    Log.d(TAG, "Msg: " + msg);
                    Log.d(TAG, "Data: " + data);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "验证码已发送！", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void logWithCode(){
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("phone", phone);
            jsonBody.put("smsCode", smscode);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        SharedPreferences sharedPreferences1 = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        TokenManager tokenManager = new TokenManager(sharedPreferences1, this);
        // 初始化OkHttpClient并添加拦截器，当接口请求报403时删除token
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jsonBody.toString());

        Request request = new Request.Builder()
                .url("https://hotfix-service-prod.g.mi.com/weibo/api/auth/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseData = response.body().string();
                Log.d(TAG, "Response: " + responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    int code = jsonResponse.getInt("code");
                    String msg = jsonResponse.getString("msg");
                    String data = jsonResponse.getString("data");
                    Log.d(TAG, "Code: " + code);
                    Log.d(TAG, "Msg: " + msg);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (code == 200){
                                //保存data为toke，下次使用间隔超过12小时删除，重新获取
                                // 发送GET请求并保存到本地
                                sendGetInfo(data);

                            }
                            else if(code == 403){
                                //删除本地缓存 跳转到登录界面
//                                Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
//                                intent.putExtra("tick",true);
//                                startActivity(intent);
//                                finish();
                            }
                            else if(code == 400){
                                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void sendGetInfo(String token){
        SharedPreferences sharedPreferences1 = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        TokenManager tokenManager = new TokenManager(sharedPreferences1, this);
        // 初始化OkHttpClient并添加拦截器，当接口请求报403时删除token
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .build();

        Request request = new Request.Builder()
                .url("https://hotfix-service-prod.g.mi.com/weibo/api/user/info")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                final String responseData = response.body().string();
                Log.d(TAG, "Response: " + responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    int code = jsonResponse.getInt("code");
                    String msg = jsonResponse.getString("msg");
                    JSONObject data = jsonResponse.getJSONObject("data");

                    int id = data.getInt("id");
                    String username = data.getString("username");
                    String phone = data.getString("phone");
                    String avatar = data.getString("avatar");
                    boolean loginStatus = data.getBoolean("loginStatus");

                    Log.d(TAG, "Code: " + code);
                    Log.d(TAG, "Msg: " + msg);
                    Log.d(TAG, "ID: " + id);
                    Log.d(TAG, "Username: " + username);
                    Log.d(TAG, "Phone: " + phone);
                    Log.d(TAG, "Avatar: " + avatar);
                    Log.d(TAG, "LoginStatus: " + loginStatus);

                    SharedPreferences preferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token);
                    editor.putBoolean("success", loginStatus);
                    editor.putString("name", username);
                    editor.putInt("follower",100);
                    editor.putString("image", avatar);
                    editor.putLong("time", System.currentTimeMillis());
                    editor.apply();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                                    intent.putExtra("tick",true);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 200);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

}
