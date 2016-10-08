package com.roundel.fizyka.dropbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by RouNdeL on 2016-09-25.
 */
public final class NotificationServiceStarterReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean("notification", false)) {
            NotificationEventReceiver.setupAlarm(context, Long.parseLong(prefs.getString("refresh_time", "240")));
        }
    }
}