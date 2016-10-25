package com.roundel.fizyka.dropbox;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.roundel.fizyka.R;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by RouNdeL on 2016-09-27.
 */
public class DropboxDownloadCompletedBroadcastReceiver extends WakefulBroadcastReceiver
{
    public static int NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        Long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

        SharedPreferences downloadPrefs = context.getSharedPreferences("download_references", Context.MODE_PRIVATE);
        long requiredReference = downloadPrefs.getLong(DropboxDownloader.DOWNLOAD_REFERENCE, 0);

        if(Objects.equals(action, DownloadManager.ACTION_DOWNLOAD_COMPLETE) && reference.equals(requiredReference))
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String folderPath = prefs.getString("download_path", "/fizyka/");
            if(folderPath.charAt(folderPath.length()-1) != '/') folderPath+="/";
            if(folderPath.charAt(0) != '/') folderPath = "/"+folderPath;

            Intent openFile = new Intent();
            openFile.setAction(android.content.Intent.ACTION_VIEW);
            File file = new File(Environment.getExternalStorageDirectory() + folderPath + context.getString(R.string.file_name));
            openFile.setDataAndType(Uri.fromFile(file), "application/zip");

            PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, openFile, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.ic_file_download_white_24dp);
            builder.setContentTitle(context.getString(R.string.download_completed_title));
            builder.setContentText(context.getString(R.string.download_completed_desc));
            builder.setColor(context.getColor(R.color.colorPrimary));
            builder.setAutoCancel(true);
            builder.setContentIntent(pendingIntent);

            final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, builder.build());

            if(prefs.getBoolean("extract", true))
            {
                String destination = Environment.getExternalStorageDirectory().getAbsolutePath()+folderPath;
                String source = destination+context.getString(R.string.file_name);

                try {
                    ZipFile zipFile = new ZipFile(source);
                    if (zipFile.isEncrypted()) {
                        throw new ZipException("File is encrypted");
                    }
                    zipFile.extractAll(destination);
                    Log.d("Zip", "Extracted successfully");
                } catch (ZipException e) {
                    e.printStackTrace();
                }
            }
            /*if(prefs.getBoolean("dates", true))
            {
                try
                {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(context.getFilesDir() + "/dropbox_entities.dat"));
                    List<DropboxEntity> entities = (List<DropboxEntity>) ois.readObject();

                    for (DropboxEntity entity : entities)
                    {
                        Long time = (entity.getDate().getTime()/1000)*1000;
                        File fileToEdit = new File(Environment.getExternalStorageDirectory() + folderPath + entity.getPath());
                        fileToEdit.setLastModified(time);
                        Log.d("TimeInMIllis", Long.toString(time));
                    }
                } catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }*/
        }
    }
}
