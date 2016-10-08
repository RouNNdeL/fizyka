package com.roundel.fizyka.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by RouNdeL on 2016-10-08.
 */
public class UpdateChecker extends AsyncTask<String, Integer, String>
{
    private UpdateCheckerListener mListener;
    private String mVersion;
    private final String TAG = "UpdateChecker";

    public UpdateChecker(UpdateCheckerListener listener)
    {
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        mListener.onTaskStart();
    }

    @Override
    protected String doInBackground(String... params)
    {
        try
        {
            URL url = new URL("https://raw.githubusercontent.com/RouNNdeL/fizyka/master/app/build.gradle");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();
            if(response != HttpURLConnection.HTTP_OK) return null;

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String currentLine;

            while (!(currentLine = br.readLine()).contains("versionName")){}
            currentLine = extractVersion(currentLine);
            mVersion = currentLine;

            return currentLine;

        }
        catch(IOException e)
        {
            //Log.e("UpdateCheck", e.getStackTrace().toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result)
    {
        mListener.onTaskEnd(result);
    }

    public String getVersion()
    {
        return mVersion;
    }

    private String extractVersion(String line)
    {
        String output = "";
        boolean inQuotation = false;
        for(char c : line.toCharArray())
        {
            if(inQuotation && c!='"') output+=c;
            if(c == '"') inQuotation = !inQuotation;
        }
        return output;
    }

    public interface UpdateCheckerListener
    {
        void onTaskStart();
        void onTaskEnd(String result);
    }
}
