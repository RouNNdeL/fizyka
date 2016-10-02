package com.roundel.fizyka;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by RouNdeL on 2016-09-27
 */
public class DownloadButtonPressedBroadcastReceiver extends WakefulBroadcastReceiver
{
    private final String ACTION_DOWNLOAD = "com.roundel.fizyka.ACTION_DOWNLOAD";
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String folderUrl = sp.getString("download_url", "https://www.dropbox.com/sh/ya38ajmh9bezwhz/AABdJ69NcP-TDN4XlnNG83t_a?dl=0");
        final String folderPath = sp.getString("download_path", "/fizyka/");
        String action = intent.getAction();
        if(action.equals(ACTION_DOWNLOAD))
        {
            editor.putString("date", intent.getStringExtra("DATE"));
            editor.apply();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
            final DropboxDownloader dropboxDownloader = new DropboxDownloader(folderUrl.replace("?dl=0", "?dl=1"), folderPath);
            dropboxDownloader.start(context);
        }
    }
}
