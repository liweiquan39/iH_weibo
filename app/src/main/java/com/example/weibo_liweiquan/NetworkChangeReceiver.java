package com.example.weibo_liweiquan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private NetworkChangeListener listener;


    public NetworkChangeReceiver(NetworkChangeListener listener) {
        this.listener = listener;

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            boolean isConnected = NetworkUtils.isNetworkConnected(context);
            if (isConnected) {
                listener.onNetworkConnected();

            } else {
                listener.onNetworkDisconnected();

            }
        }
    }
    public interface NetworkChangeListener {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }

}
