package com.roundel.fizyka.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.roundel.fizyka.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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
    private Context mContext;

    public DropboxLinkValidator(Context context, DropboxLinkValidatorListener listener)
    {
        this.mListener = listener;
        this.mContext = context;
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
        try
        {
            urlString += "?link=" + URLEncoder.encode(urlShared, "UTF-8");
            URL url = new URL(urlString);
            JSONObject jsonData = new JSONObject();
            jsonData.put("link", urlShared);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer " + mContext.getString(R.string.api_token));

            // Send POST data.
            DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
            printout.writeBytes(jsonData.toString());
            printout.flush();
            printout.close();

            int HttpResponseCode = urlConnection.getResponseCode();
            Log.d("DropboxMetadata", urlConnection.getResponseMessage());

            if(HttpResponseCode == HttpURLConnection.HTTP_OK)
            {
                return NO_ERROR;
            }
            else if(HttpResponseCode == HttpURLConnection.HTTP_FORBIDDEN)
            {
                return ERROR_FORBIDDEN;
            }
            else if(HttpResponseCode == HttpURLConnection.HTTP_NOT_FOUND)
            {
                return ERROR_NOT_FOUND;
            }
            else
            {
                return ERROR_UNKNOWN;
            }

        }
        catch(IOException | JSONException e)
        {
            Log.e(TAG, e.getMessage());
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
