package com.roundel.fizyka.update;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.roundel.fizyka.R;

import java.io.File;

/**
 * Created by RouNdeL on 2016-10-08.
 */
public class UpdateDownloader
{
    public final static String ACTION_UPDATE = "com.roundel.fizyka.ACTION_UPDATE";
    public final static String DOWNLOAD_REFERENCE = "update_reference";
    public final static String DOWNLOAD_VERSION = "update_version";
    public final static int NOTIFICATION_UPDATE_DOWNLOADING = 3;
    public final static int NOTIFICATION_UPDATE_DOWNLOADED = 4;
    public final static int NOTIFICATION_UPDATE_ERROR = 5;
    public final static int NOTIFICATION_UPDATE = 6;

    private long mDownloadReference;
    private String mNewVersion;

    public UpdateDownloader(String version)
    {
        this.mNewVersion = version;
    }

    public void start(Context context)
    {
        File file = new File(context.getExternalFilesDir(null)+"/"+context.getString(R.string.update_file_name));
        if(file.exists()) file.delete();

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(context.getString(R.string.update_url)+"app-release.apk");
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(context.getString(R.string.update_title));
        request.setDescription(context.getString(R.string.download_disc));
        request.setDestinationInExternalFilesDir(context, "", context.getString(R.string.update_file_name));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.allowScanningByMediaScanner();
        request.setMimeType("application/vnd.android.package-archive");
        request.setVisibleInDownloadsUi(false);
        mDownloadReference = downloadManager.enqueue(request);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_sync_black_24dp)
                .setColor(context.getColor(R.color.colorPrimary))
                .setContentTitle(context.getString(R.string.update_in_progress_title))
                .setContentText(String.format(context.getString(R.string.update_in_progress_disc), mNewVersion))
                .setProgress(0,0,true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_UPDATE_DOWNLOADING, builder.build());
    }

    public long getDownloadReference()
    {
        return mDownloadReference;
    }

    public static boolean checkIfNew(String newVersion, String oldVersion)
    {
        String newVersionNum = Character.isLetter(newVersion.charAt(newVersion.length()-1))?newVersion.substring(0,newVersion.length()-1):newVersion;
        String oldVersionNum = Character.isLetter(oldVersion.charAt(oldVersion.length()-1))?oldVersion.substring(0,oldVersion.length()-1):oldVersion;

        return newVersionNum.compareTo(oldVersionNum)==0?newVersion.compareTo(oldVersion)>0:newVersionNum.compareTo(oldVersionNum)>0;
    }
}
