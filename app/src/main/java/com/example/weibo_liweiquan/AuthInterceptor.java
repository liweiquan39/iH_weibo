package com.example.weibo_liweiquan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthInterceptor implements Interceptor {

    private TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        if (response.code() == 403) {
            // 清空token
            tokenManager.clearToken();
        }

        return response;
    }
}


class TokenManager {
    private SharedPreferences sharedPreferences;
    private Context context;

    public TokenManager(SharedPreferences sharedPreferences,Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(context, HomePageActivity.class);
        intent.putExtra("tick",true);
        context.startActivity(intent);
        ((Activity)context).finish();
    }

}
