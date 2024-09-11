package com.example.weibo_liweiquan;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FlowDataSource {
    public static List<FlowItem> ITEMS = new ArrayList<>();
    public static List<WeiboInfo> GAMES = new ArrayList<>();
    private static final String TAG = "FlowDataSource";
    private static final String URL = "https://hotfix-service-prod.g.mi.com/weibo/homePage";
    public static String token = "";
    static int curPage=0;


    private static void addItem(FlowItem items){ITEMS.add(items);}
    public static List<FlowItem> loadItems(Context context, String t){
        token = t;
        ITEMS=new ArrayList<>();
        try {
            httpget2(context);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Collections.shuffle(GAMES);
        ITEMS = new ArrayList<>();
        displayData(GAMES);

//        if (GAMES.size()/2 <= 0) return new ArrayList<>();
//        int offset = new Random().nextInt(GAMES.size()-1);
//        List<WeiboInfo> listgame = new ArrayList<>(GAMES.subList(offset,GAMES.size()));
//        for(int i=0;i<offset;i++){
//            listgame.add(GAMES.get(i));
//        }
//        GAMES = new ArrayList<>(listgame.subList(0,listgame.size()));
//        if (offset>0){
//            ITEMS = new ArrayList<>();
//            displayData(GAMES);
//        }
//        Log.i(TAG, String.valueOf(ITEMS.size()));


        return new ArrayList<>(ITEMS);
    }
    public static List<FlowItem> refresh(Context context){
        curPage=0;
        if(homeFragment.networkStatus){
            Collections.shuffle(GAMES);
            ITEMS = new ArrayList<>();
            displayData(GAMES);

            Gson gson = new Gson();
            String json = gson.toJson(GAMES);
            SharedPreferences sharedP = context.getSharedPreferences("dataCache", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedP.edit();
            editor.clear();
            editor.apply();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    editor.putString("dataList", json);
                    editor.apply();
                }
            }).start();
        }
        else{
            SharedPreferences sharedP = context.getSharedPreferences("dataCache", Context.MODE_PRIVATE);
            String json = sharedP.getString("dataList", null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<WeiboInfo>>() {}.getType();
                List<WeiboInfo> list_fail = gson.fromJson(json, type);

                int offset = new Random().nextInt(list_fail.size()-1);
                List<WeiboInfo> listgame = new ArrayList<>(list_fail.subList(offset,list_fail.size()));
                for(int i=0;i<offset;i++){
                    listgame.add(list_fail.get(i));
                }
                GAMES = new ArrayList<>(listgame.subList(0,listgame.size()));
                if (offset>0){
                    ITEMS = new ArrayList<>();
                    displayData(GAMES);
                }
            }
        }


        return ITEMS;
    }
    public static List<FlowItem> refreshget(Context context, String t) {
        curPage=0;
        if(homeFragment.networkStatus){ //Get
            token = t;
            ITEMS=new ArrayList<>();
            try {
                httpget2(context);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Collections.shuffle(GAMES);
            ITEMS = new ArrayList<>();
            displayData(GAMES);

            Gson gson = new Gson();
            String json = gson.toJson(GAMES);
            SharedPreferences sharedP = context.getSharedPreferences("dataCache", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedP.edit();
            editor.clear();
            editor.apply();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    editor.putString("dataList", json);
                    editor.apply();
                }
            }).start();
        }
        else{
            SharedPreferences sharedP = context.getSharedPreferences("dataCache", Context.MODE_PRIVATE);
            String json = sharedP.getString("dataList", null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<WeiboInfo>>() {}.getType();
                List<WeiboInfo> list_fail = gson.fromJson(json, type);

                int offset = new Random().nextInt(list_fail.size()-1);
                List<WeiboInfo> listgame = new ArrayList<>(list_fail.subList(offset,list_fail.size()));
                for(int i=0;i<offset;i++){
                    listgame.add(list_fail.get(i));
                }
                GAMES = new ArrayList<>(listgame.subList(0,listgame.size()));
                if (offset>0){
                    ITEMS = new ArrayList<>();
                    displayData(GAMES);
                }
            }
        }

        return ITEMS;
    }
    public static List<FlowItem> loadMore(){
        Log.i("loadmoreover", String.valueOf(ITEMS.size()));
        if(curPage++<4){
            return new ArrayList<>(ITEMS);
        } else {
            return new ArrayList<>();
        }
    }

    public static List<WeiboInfo> loadDataItems(){
        return GAMES;
    }
    private static void httpget2(Context context) throws InterruptedException {  //OkHttp 网络请求
        SharedPreferences sharedPreferences1 = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        TokenManager tokenManager = new TokenManager(sharedPreferences1,context);
        // 初始化OkHttpClient并添加拦截器，当接口请求报403时删除token
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenManager))
                .build();

        Request.Builder requestBuilder = new Request.Builder().url(URL);
        CountDownLatch latch = new CountDownLatch(1);
        if (!Objects.equals(token, "")) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                latch.countDown();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseData = response.body().string();
                Gson gson = new Gson();

                WeiboResponse weiboResponse;
                try {
                    weiboResponse = gson.fromJson(responseData, WeiboResponse.class);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    return;
                }

                if (weiboResponse != null && weiboResponse.getCode() == 200) {
                    List<WeiboInfo> weiboBeans = weiboResponse.getData().getRecords();
                    GAMES = weiboBeans;
                    displayData(weiboBeans);

                } else {
                    Log.i(TAG, "Failed to parse data!");
                }
                latch.countDown();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }
    private static void displayData(List<WeiboInfo> weiboBeans) {
        for (WeiboInfo weibo : weiboBeans) {
            addItem(new Item(weibo));
        }
    }
    public static List<FlowItem> setFailData(List<WeiboInfo> list_fail) {
        GAMES = new ArrayList<>(list_fail);
        ITEMS = new ArrayList<>();
        for (WeiboInfo weibo : list_fail) {
            addItem(new Item(weibo));
        }
        return ITEMS;
    }

}
