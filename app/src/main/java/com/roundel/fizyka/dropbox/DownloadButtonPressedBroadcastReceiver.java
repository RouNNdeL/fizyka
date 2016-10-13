package com.roundel.fizyka.dropbox;


import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.roundel.fizyka.dropbox.DropboxDownloadCompletedBroadcastReceiver;
import com.roundel.fizyka.dropbox.DropboxDownloader;

/**
 * Created by RouNdeL on 2016-09-27
 */
public class DownloadButtonPressedBroadcastReceiver extends WakefulBroadcastReceiver
{


    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String folderUrl = sp.getString("download_url", "https://www.dropbox.com/sh/ya38ajmh9bezwhz/AABdJ69NcP-TDN4XlnNG83t_a?dl=0");
        final String folderPath = sp.getString("download_path", "/fizyka/");
        String action = intent.getAction();
        if(action.equals(DropboxDownloader.ACTION_DOWNLOAD))
        {
            editor.putString("date", intent.getStringExtra("DATE"));
            editor.apply();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);

            final DropboxDownloader downloader = new DropboxDownloader(folderUrl, folderPath);
            downloader.start(context);

            SharedPreferences.Editor downloadPrefsEditor = context.getSharedPreferences("download_references", Context.MODE_PRIVATE).edit();

            downloadPrefsEditor.putLong(DropboxDownloader.DOWNLOAD_REFERENCE, downloader.getDownloadReference());
            downloadPrefsEditor.apply();
        }
    }
}
