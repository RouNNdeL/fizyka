package com.roundel.fizyka.dropbox;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by RouNdeL on 2016-10-05.
 */
public class DropboxLinkValidator extends AsyncTask<String, String, String>
{

    public final static String NO_ERROR = "NO_ERROR";
    public final static String ERROR_FORBIDDEN = "ERROR_FORBIDDEN";
    public final static String ERROR_NOT_FOUND = "ERROR_NOT_FOUND";
    public final static String ERROR_UNKNOWN = "ERROR_UNKNOWN";
    public final static String ERROR_CONNECTION_TIMED_OUT = "ERROR_CONNECTION_TIMED_OUT";

    private final String TAG = "DropboxLinkValidator";

    private DropboxLinkValidatorListener mListener;

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
        String urlString = params[0];
        String urlShared = params[1];
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
                return NO_ERROR;
            }
            else if(HttpResponseCode == HttpURLConnection.HTTP_FORBIDDEN)
            {
                return ERROR_FORBIDDEN;
            }
            else if (HttpResponseCode == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return ERROR_NOT_FOUND;
            }
            else
            {
                return ERROR_UNKNOWN;
            }

        }
        catch (IOException|JSONException e)
        {
            Log.e(TAG, Arrays.toString(e.getStackTrace()));
        }
        return ERROR_CONNECTION_TIMED_OUT;
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
