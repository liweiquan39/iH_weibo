package com.example.weibo_liweiquan;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class mineFragment extends Fragment {

    private ImageView avatarImageView;
    private TextView usernameTextView, followersTextView, logoutTextView, whiteTextView;
    private SharedPreferences sharedPreferences;
    private boolean isLoggedIn;
    private static final String TAG = "mine";

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);

        avatarImageView = view.findViewById(R.id.avatar_image_view);
        usernameTextView = view.findViewById(R.id.username_text_view);
        followersTextView = view.findViewById(R.id.followers_text_view);
        logoutTextView = view.findViewById(R.id.logout_text);
        whiteTextView = view.findViewById(R.id.white_text);
        sharedPreferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("success", false);
        //判断是否超过12小时过期
        if(isDataExpired()) clearData();

        if (isLoggedIn && NetworkUtils.isNetworkConnected(requireContext())) {
            String minetoken = sharedPreferences.getString("token","");
            sendGetInfomine(minetoken);

            int followersCount = sharedPreferences.getInt("follower",0);
            usernameTextView.setText(sharedPreferences.getString("name",""));
            Glide.with(mineFragment.this)
                    .load(sharedPreferences.getString("image",""))
                    .into(avatarImageView);
            if (followersCount != 0) {
                followersTextView.setText("粉丝 " + followersCount);
            } else {
                followersTextView.setVisibility(View.GONE);
            }

            //if 动态数 == 0 : setText;  else  setVisiblity;(展示动态，也是Recycle)
            whiteTextView.setText("你没有新动态哦~");
            logoutTextView.setVisibility(View.VISIBLE);
            logoutTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "已退出账号！", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();
                    notLogIn();
                }
            });

        } else {
            notLogIn();
        }

        return view;
    }
    public void notLogIn(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        whiteTextView.setText("登录后查看");
        usernameTextView.setText("点击头像登录");
        followersTextView.setText("点击头像去登录");
        logoutTextView.setVisibility(View.GONE);
        avatarImageView.setImageResource(R.drawable.default_avatar);
        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private boolean isDataExpired() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        long timestamp = preferences.getLong("time", 0);
        return System.currentTimeMillis() - timestamp > (long)12 * 60 * 60 * 1000;
    }

    private void clearData() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    private void sendGetInfomine(String token){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://hotfix-service-prod.g.mi.com/weibo/api/user/info")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // 请求失败处理
                e.printStackTrace();
                Log.e(TAG, "Request failed: " + e.getMessage());
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

                    SharedPreferences preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token);
                    editor.putBoolean("success", loginStatus);
                    editor.putString("name", username);
                    editor.putInt("follower",100);
                    editor.putString("image", avatar);
                    editor.putLong("time", System.currentTimeMillis());
                    editor.apply();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
