package com.roundel.fizyka;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by RouNdeL on 2016-10-05.
 */
public class DropboxLinkValidator extends AsyncTask<String, String, String>
{
    DropboxLinkValidatorListener mListener;
    public DropboxLinkValidator(DropboxLinkValidatorListener listener)
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
        String urlShared = params[1];
        String urlString = params[0];
        String urlPath = "/";
        urlString += "?link="+urlShared+"&path="+urlPath;
        try {
            URL url = new URL(urlString);
            JSONObject jsonData = new JSONObject();
            jsonData.put("link", urlShared);
            jsonData.put("path", urlPath);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer CagAdvw3mIMAAAAAAAAEz3dumqld_1pSkukJNYeTCabJjq_WXmLpdtAFT_RlJP0t");

            // Send POST data.
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(jsonData.toString());
            printout.flush();
            printout.close();

            int HttpResponseCode = urlConnection.getResponseCode();

            if(HttpResponseCode == HttpURLConnection.HTTP_OK)
            {
                return DropboxMetadata.NO_ERROR;
            }
            else if(HttpResponseCode == HttpURLConnection.HTTP_FORBIDDEN)
            {
                return DropboxMetadata.ERROR_FORBIDDEN;
            }
            else if (HttpResponseCode == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return DropboxMetadata.ERROR_NOT_FOUND;
            }
            else
            {
                return DropboxMetadata.ERROR_UNKNOWN;
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }

        return DropboxMetadata.ERROR_CONNECTION_TIMED_OUT;
    }

    @Override
    protected void onPostExecute(String result)
    {
        mListener.onTaskEnd(result);
    }

    public interface DropboxLinkValidatorListener
    {
        void onTaskStart();
        void onTaskEnd(String result);
    }
}
