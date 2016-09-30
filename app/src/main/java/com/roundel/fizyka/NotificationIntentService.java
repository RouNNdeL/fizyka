package com.roundel.fizyka;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class NotificationIntentService extends IntentService
{

    private static final int NOTIFICATION_ID = 1;
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_DELETE = "ACTION_DELETE";
    private Date mRecentUpdate;
    private String mFolderUrl;

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
    public static DateFormat mDropboxDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private Date newestDate(List<String> array)
    {
        try{
            Date newest = mDropboxDateFormat.parse(array.get(0));
            Date current;
            for (int i = 0; i < array.size(); i++)
            {
                current = mDropboxDateFormat.parse(array.get(i));
                if(newest.before(current))
                {
                    newest = current;
                }
            }
            return newest;
        }
        catch (ParseException e)
        {
            Log.e("DATES", e.getMessage());
            return new Date();
        }

    }
    private void processStartNotification() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mFolderUrl = sp.getString("download_url", "https://www.dropbox.com/sh/ya38ajmh9bezwhz/AABdJ69NcP-TDN4XlnNG83t_a?dl=0");
        try
        {

            mRecentUpdate = mDropboxDateFormat.parse(sp.getString("date", "Thu, 01 Jan 1970 00:00:00 +0000"));
        }catch (ParseException e)
        {
            Log.d("DATE", e.getMessage());
        }
        DropboxMetadata dropboxMetadata= new DropboxMetadata(new DropboxMetadata.DropboxMetadataListener()
        {
            @Override
            public void onTaskEnd(List<String> result)
            {
                Date date = newestDate(result);
                if(date.after(mRecentUpdate))
                {
                    Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Intent downloadIntent = new Intent();
                    downloadIntent.setAction("ACTION_DOWNLOAD");
                    downloadIntent.putExtra("DATE", mDropboxDateFormat.format(date));
                    PendingIntent pendingDownloadIntent = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_ID, downloadIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                    builder.setContentTitle(getString(R.string.notify_title))
                            .setAutoCancel(true)
                            .setColor(getColor(R.color.colorPrimary))
                            .setContentText(getString(R.string.notify_desc))
                            .setSmallIcon(R.drawable.ic_cloud_download_white_24dp)
                            .addAction(R.drawable.ic_file_download_white_24dp, getString(R.string.notify_button), pendingDownloadIntent)
                            .setSound(uri);

                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, new Intent(getApplicationContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);
                    builder.setDeleteIntent(NotificationEventReceiver.getDeleteIntent(getApplicationContext()));

                    final NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_ID, builder.build());
                }
                else
                {
                    Log.d("NOTIFY", "No update");
                }
            }

            @Override
            public void onTaskStart()
            {

            }
        });
        dropboxMetadata.execute(getString(R.string.api_url), mFolderUrl, "/");


    }
}