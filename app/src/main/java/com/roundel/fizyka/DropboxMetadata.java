package com.roundel.fizyka;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by RouNdeL on 2016-09-24.
 */
public class DropboxMetadata extends AsyncTask <String, Integer, String>
{
    private final String TAG = "TASK";

    public List<String> datesList = new ArrayList<String>();

    public DropboxMetadataListener listener = null;
    private DateFormat dateFormat;
    public final static String NO_ERROR = "NO_ERROR";
    public final static String ERROR_FORBIDDEN = "ERROR_FORBIDDEN";
    public final static String ERROR_NOT_FOUND = "ERROR_NOT_FOUND";
    public final static String ERROR_UNKNOWN = "ERROR_UNKNOWN";
    private String errorState = NO_ERROR;

    public DropboxMetadata(DateFormat dateFormat, DropboxMetadataListener listener)
    {
        this.dateFormat = dateFormat;
        this.listener=listener;
    }

    @Override
    protected String doInBackground(String... params)
    {
        checkSubdirectories(params[0], params[1], params[2]);
        if(!errorState.equals(NO_ERROR)) return errorState;
        return dateFormat.format(newestDate(datesList));
    }

    @Override
    protected void onPostExecute(String s)
    {
        listener.onTaskEnd(s);
    }

    @Override
    protected void onPreExecute()
    {
        listener.onTaskStart();
    }
    protected void checkSubdirectories(String url, String folder, String path)
    {
        String response = singlePOST(url, folder, path);
        if(!errorState.equals(NO_ERROR)) return;
        //Log.d(TAG, response);
        try{
            JSONObject jsonObject = new JSONObject(response);
            datesList.add(jsonObject.getString("modified"));
            if(jsonObject.has("contents"))
            {
                JSONArray arr = jsonObject.getJSONArray("contents");
                for(int i = 0; i < arr.length(); i++ )
                {
                    checkSubdirectories(url, folder, arr.getJSONObject(i).getString("path"));
                }
            }

        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }
    protected String singlePOST(String urlString, String urlShared, String urlPath)
    {
        String resultToDisplay = "";
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
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                resultToDisplay = br.readLine();
            }
            else if(HttpResponseCode == HttpURLConnection.HTTP_FORBIDDEN)
            {
                Log.e(TAG, "Access denied");
                errorState = ERROR_FORBIDDEN;
                return errorState;
            }
            else if (HttpResponseCode == HttpURLConnection.HTTP_NOT_FOUND)
            {
                Log.e(TAG, "Wrong url");
                errorState = ERROR_NOT_FOUND;
                return errorState;
            }
            else
            {
                Log.e(TAG, "Unknown error");
                errorState = ERROR_UNKNOWN;
                return errorState;
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }

        return resultToDisplay;
    }

    private Date newestDate(List<String> array)
    {
        try{
            Date newest = dateFormat.parse(array.get(0));
            Date current;
            for (int i = 0; i < array.size(); i++)
            {
                current = dateFormat.parse(array.get(i));
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

    public interface DropboxMetadataListener
    {
        void onTaskStart();
        void onTaskEnd(String result);
    }

    //Might use that later
    /*protected boolean checkConnection()
    {
       try
       {
           URL url = new URL("http://www.gstatic.com/generate_204");
           HttpURLConnection connection = (HttpURLConnection) url.openConnection();
           connection.setRequestMethod("GET");
           return connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT;
       }
       catch (IOException e)
       {
           Log.e(TAG, e.getMessage());
       }
        return false;
    }*/
}
