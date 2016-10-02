package com.roundel.fizyka;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by RouNdeL on 2016-10-02.
 */
public class ConnectionStateChangedReceiver extends WakefulBroadcastReceiver
{
    public final  String TAG = "CONNECTION";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION)
        {
            Log.d(TAG, "Connection change");
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if(info != null && info.isConnected())
            {
                Log.d(TAG, "Connection established, starting the NotificationIntentService");
                Intent serviceIntent = NotificationIntentService.createIntentStartNotificationService(context);
                startWakefulService(context, serviceIntent);

                ComponentName receiver = new ComponentName(context, ConnectionStateChangedReceiver.class);

                PackageManager pm = context.getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        }
    }
}
