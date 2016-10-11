package com.roundel.fizyka.update;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.roundel.fizyka.R;
import com.roundel.fizyka.dropbox.DropboxDownloader;

import java.io.File;
import java.util.Objects;

/**
 * Created by RouNdeL on 2016-10-08.
 */
public class UpdateDownloadCompletedBroadcastReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        SharedPreferences downloadPrefs = context.getSharedPreferences("download_references", Context.MODE_PRIVATE);
        long requiredReference = downloadPrefs.getLong(UpdateDownloader.DOWNLOAD_REFERENCE, 0);
        String version = downloadPrefs.getString(UpdateDownloader.DOWNLOAD_VERSION, "0");

        if(Objects.equals(action, DownloadManager.ACTION_DOWNLOAD_COMPLETE) && Objects.equals(reference, requiredReference))
        {
            File file = new File(context.getExternalFilesDir(null)+"/"+context.getString(R.string.update_file_name));
            PackageManager packageManager = context.getPackageManager();
            PackageInfo info = packageManager.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);


            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(UpdateDownloader.NOTIFICATION_UPDATE_DOWNLOADING);

            if (Objects.equals(info.versionName, version))
            {
                Intent installIntent = new Intent();
                installIntent.setAction(Intent.ACTION_VIEW);
                installIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");

                PendingIntent pendingIntent = PendingIntent.getActivity(context, UpdateDownloader.NOTIFICATION_UPDATE_DOWNLOADED, installIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_file_download_white_24dp)
                        .setContentTitle(context.getString(R.string.update_completed_title))
                        .setContentText(String.format(context.getString(R.string.update_completed_disc), version))
                        .setColor(context.getColor(R.color.colorPrimary))
                        .addAction(R.drawable.ic_system_update_black_24dp, context.getString(R.string.update_notify_button), pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(String.format(context.getString(R.string.update_completed_disc_expanded), version)));

                manager.notify(UpdateDownloader.NOTIFICATION_UPDATE_DOWNLOADED, builder.build());
            }
            else
            {
                String versionInstalled = "";
                try
                {
                    versionInstalled = packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
                }
                catch (PackageManager.NameNotFoundException e)
                {}
                String body = String.format("Expected version: %s\nVersion found: %s\nVersion installed: %s", version, info.versionName, versionInstalled);
                Intent sendIntent = new Intent();
                sendIntent.setData(Uri.parse("mailto:rounndel@gmail.com"));
                sendIntent.setAction(Intent.ACTION_SENDTO);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Update error");
                sendIntent.putExtra(Intent.EXTRA_TEXT, body);

                PendingIntent pendingIntent = PendingIntent.getActivity(context, UpdateDownloader.NOTIFICATION_UPDATE_ERROR, sendIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setSmallIcon(R.drawable.ic_error_outline_black_24dp)
                        .setContentTitle(context.getString(R.string.update_error_wrong_version_title))
                        .setContentText(context.getString(R.string.update_error_wrong_version_disc))
                        .setColor(context.getColor(R.color.colorPrimary))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(context.getString(R.string.update_error_wrong_version_disc_long)))
                        .addAction(R.drawable.ic_send_black_24dp, context.getString(R.string.update_error_notify_button), pendingIntent);

                manager.notify(UpdateDownloader.NOTIFICATION_UPDATE_ERROR, builder.build());
            }
        }
    }
}
