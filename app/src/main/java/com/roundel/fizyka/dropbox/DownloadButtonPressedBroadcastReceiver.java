package com.roundel.fizyka.dropbox;


import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.List;

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
            String jsonEntities = intent.getStringExtra("ENTITIES");
            Type type = new TypeToken<List<DropboxEntity>>()
            {
            }.getType();
            List<DropboxEntity> entitiesToSave = (new Gson()).fromJson(jsonEntities, type);
            try
            {
                FileOutputStream out = new FileOutputStream(context.getFilesDir() + "/dropbox_entities.dat");
                ObjectOutputStream oout = new ObjectOutputStream(out);
                oout.writeObject(entitiesToSave);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

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
