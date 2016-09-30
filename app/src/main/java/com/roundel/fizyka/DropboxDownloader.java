package com.roundel.fizyka;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by RouNdeL on 2016-09-24.
 */
public class DropboxDownloader
{
    private String TAG = "FILE";
    private String mDownloadURL;
    private String mPath;
    private DownloadManager downloadManager;
    public long mDownloadReference;

    private Float mDownloadProgress;

    public DropboxDownloader(String url, String path)
    {
        mDownloadURL = url;
        mPath = path;
    }
    public void start(Context context)
    {
        String filesDir = mPath;
        File file = new File(Environment.getExternalStorageDirectory()+mPath+"fizyka.zip");
        File fileOld = new File(Environment.getExternalStorageDirectory()+mPath+"fizyka.zip");
        if(fileOld.exists())
        {
            fileOld.delete();
        }
        if(file.exists())
        {
            file.renameTo(new File(filesDir+"fizyka.zip.old"));
            file.delete();
        }
        downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(mDownloadURL);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(context.getString(R.string.download_title));
        request.setDescription(context.getString(R.string.download_disc));
        request.setDestinationInExternalPublicDir(mPath, "fizyka.zip");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.allowScanningByMediaScanner();
        request.setMimeType("application/zip");
        request.setVisibleInDownloadsUi(false);
        mDownloadReference = downloadManager.enqueue(request);
    }

    public long getDownloadReference()
    {
        return mDownloadReference;
    }
}
