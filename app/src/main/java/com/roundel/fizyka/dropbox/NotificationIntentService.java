package com.roundel.fizyka.dropbox;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.roundel.fizyka.Connectivity;
import com.roundel.fizyka.R;
import com.roundel.fizyka.activity.FileExplorerActivity;
import com.roundel.fizyka.activity.SettingsActivity;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class NotificationIntentService extends IntentService
{

    public static final int NOTIFICATION_ID = 1;
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_DELETE = "ACTION_DELETE";
    private Date mRecentUpdate;

    public NotificationIntentService() {
        super(NotificationIntentService.class.getSimpleName());
    }

    public static Intent createIntentStartNotificationService(Context context) {
        Intent intent = new Intent(context, NotificationIntentService.class);
        intent.setAction(ACTION_START);
        return intent;
    }

    public static Intent createIntentDeleteNotification(Context context) {
        Intent intent = new Intent(context, NotificationIntentService.class);
        intent.setAction(ACTION_DELETE);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(getClass().getSimpleName(), "onHandleIntent, started handling a notification event");
        try {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                processStartNotification();
            }
            if (ACTION_DELETE.equals(action)) {
                processDeleteNotification(intent);
            }
        } finally {
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    private void processDeleteNotification(Intent intent) {
        // Log something?
    }
    private void processStartNotification() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        final String mFolderUrl = sp.getString("download_url", "https://www.dropbox.com/sh/ya38ajmh9bezwhz/AABdJ69NcP-TDN4XlnNG83t_a?dl=0");
        try
        {
            mRecentUpdate = SettingsActivity.mDropboxDateFormat.parse(sp.getString("date", "Thu, 01 Jan 1970 00:00:00 +0000"));
        }catch (ParseException e)
        {
            Log.d("DATE", e.getMessage());
        }
        Connectivity.hasAccess(new Connectivity.onHasAccessResponse()
        {
            @Override
            public void onConnectionCheckStart()
            {

            }

            @Override
            public void onConnectionAvailable(Long responseTime)
            {
                DropboxMetadata dropboxMetadata= new DropboxMetadata(SettingsActivity.mDropboxDateFormat, getApplicationContext(), new DropboxMetadata.DropboxMetadataListener()
                {
                    @Override
                    public void onTaskEnd(List<DropboxEntity> result)
                    {
                        try
                        {
                            File file = new File(getFilesDir() + "/dropbox_entities.dat");
                            if (!file.exists()) file.createNewFile();

                            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir() + "/dropbox_entities.dat"));
                            List<DropboxEntity> oldEntities = (List<DropboxEntity>) ois.readObject();

                            List<DropboxEntity> newEntities = DropboxEntity.getNewEntities(oldEntities, result);

                            if (newEntities.size() > 0)
                            {
                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Intent downloadIntent = new Intent();
                                downloadIntent.setAction(DropboxDownloader.ACTION_DOWNLOAD);
                                String jsonEntities = (new Gson()).toJson(result);

                                downloadIntent.putExtra("ENTITIES", jsonEntities);
                                PendingIntent pendingDownloadIntent = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_ID, downloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                                        .setBigContentTitle(getString(R.string.notify_title));
                                for(DropboxEntity entity : newEntities)
                                {
                                    if(entity.getType() == DropboxEntity.TYPE_FILE) style
                                            .addLine(entity.getName()+" "
                                                    +getString(R.string.notify_changelog_in) +" "
                                                    +entity.getParentDirectory().replace("/", ""));
                                }

                                if(newEntities.size() > 7) style.setSummaryText(String.format(getString(R.string.notify_changelog_more), newEntities.size()-7));

                                final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                                builder.setContentTitle(getString(R.string.notify_title))
                                        .setAutoCancel(false)
                                        .setColor(getColor(R.color.colorPrimary))
                                        .setContentText(getString(R.string.notify_desc))
                                        .setStyle(style)
                                        .setSmallIcon(R.drawable.ic_cloud_download_white_24dp)
                                        .addAction(R.drawable.ic_file_download_white_24dp, getString(R.string.download_notify_button), pendingDownloadIntent)
                                        .setSound(uri);

                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, new Intent(getApplicationContext(), FileExplorerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(pendingIntent);
                                builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(getApplicationContext()));

                                final NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                manager.notify(NOTIFICATION_ID, builder.build());
                            } else
                            {
                                Log.d("IntentService", "No update");
                            }

                            /*Date date = mDropboxDateFormat.parse("");
                            mNewRecentDate = date.after(mRecentUpdate) ? date : mRecentUpdate;
                            showDownloadDialog(date.after(mRecentUpdate));*/
                        } catch (ClassNotFoundException | EOFException e)
                        {
                            try
                            {
                                File file = new File(getFilesDir() + "/dropbox_entities.dat");
                                if (!file.exists()) file.createNewFile();

                                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                Intent downloadIntent = new Intent();
                                downloadIntent.setAction(DropboxDownloader.ACTION_DOWNLOAD);

                                String jsonEntities = (new Gson()).toJson(result);
                                downloadIntent.putExtra("ENTITIES", jsonEntities);
                                PendingIntent pendingDownloadIntent = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_ID, downloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                                NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                                        .setBigContentTitle(getString(R.string.notify_title));
                                for(DropboxEntity entity : result)
                                {
                                    if(entity.getType() == DropboxEntity.TYPE_FILE) style
                                            .addLine(entity.getName()+" "
                                                    +getString(R.string.notify_changelog_in) +" "
                                                    +entity.getParentDirectory().replace("/", ""));
                                }

                                if(result.size() > 7) style.setSummaryText(String.format(getString(R.string.notify_changelog_more), result.size()-7));

                                final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                                builder.setContentTitle(getString(R.string.notify_title))
                                        .setAutoCancel(false)
                                        .setColor(getColor(R.color.colorPrimary))
                                        .setContentText(getString(R.string.notify_desc))
                                        .setSmallIcon(R.drawable.ic_cloud_download_white_24dp)
                                        /*.setStyle(new NotificationCompat.BigTextStyle()
                                                .bigText(getString(R.string.notify_desc_initial)))*/
                                        .setStyle(style)
                                        .addAction(R.drawable.ic_file_download_white_24dp, getString(R.string.download_notify_button), pendingDownloadIntent)
                                        .setSound(uri);

                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, new Intent(getApplicationContext(), FileExplorerActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(pendingIntent);
                                builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(getApplicationContext()));

                                final NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                manager.notify(NOTIFICATION_ID, builder.build());

                            } catch (IOException e1)
                            {
                                Log.e("ReadError1", "", e1);
                            }
                        } catch (IOException e2)
                        {
                            Log.e("ReadError1", "", e2);
                        }
                    }

                    @Override
                    public void onTaskStart()
                    {

                    }
                });
                dropboxMetadata.execute(getString(R.string.api_url), mFolderUrl, "/");
            }

            @Override
            public void onConnectionUnavailable()
            {
                Log.d("NOTIFY", "No network connection, postponing the NotificationIntentService");
                ComponentName receiver = new ComponentName(getApplicationContext(), ConnectionStateChangedReceiver.class);

                PackageManager pm = getApplicationContext().getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }
        });
    }
}