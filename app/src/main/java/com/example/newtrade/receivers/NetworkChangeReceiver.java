// app/src/main/java/com/example/newtrade/receivers/NetworkChangeReceiver.java
package com.example.newtrade.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Network change detected");

        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = networkInfo != null && networkInfo.isConnected();

                Log.d(TAG, "Network connected: " + isConnected);

                // Broadcast network status
                Intent networkIntent = new Intent("com.example.newtrade.NETWORK_CHANGED");
                networkIntent.putExtra("isConnected", isConnected);
                context.sendBroadcast(networkIntent);
            }
        }
    }
}