package com.roundel.fizyka.dropbox;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.roundel.fizyka.R;

import java.io.File;
import java.util.Objects;

/**
 * Created by RouNdeL on 2016-09-24.
 */
public class DropboxDownloader
{
    public final static String ACTION_DOWNLOAD = "com.roundel.fizyka.ACTION_DOWNLOAD";
    public final static String DOWNLOAD_REFERENCE = "dropbox_download_reference";

    private String mDownloadURL;
    private String mPath;
    private long mDownloadReference;

    public DropboxDownloader(String url, String path)
    {

        url = url.replace(" ", "");
        if(!url.contains("dl=1"))
        {
            if(url.contains("dl=0")) url = url.replace("dl=0", "dl=1");
            else if(url.contains("?"))
            {
                if(Objects.equals(url.charAt(url.length()-1), '?')) url+="dl=1";
                else url+="&dl=1";
            }
            else url+="?dl=1";
        }
        //Log.d("DropboxDownloader", url);
        mDownloadURL = url;
        mPath = path;
    }
    public void start(Context context)
    {
        File file = new File(Environment.getExternalStorageDirectory()+mPath+context.getString(R.string.file_name));
        if(file.exists()) file.delete();

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(mDownloadURL);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(context.getString(R.string.download_title));
        request.setDescription(context.getString(R.string.download_disc));
        request.setDestinationInExternalPublicDir(mPath, context.getString(R.string.file_name));
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
