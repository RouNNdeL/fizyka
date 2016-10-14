package com.roundel.fizyka.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.roundel.fizyka.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by RouNdeL on 2016-09-24.
 */
public class DropboxMetadata extends AsyncTask <String, Integer, List<DropboxEntity>>
{
    private final String TAG = "DropboxTask";

    public List<DropboxEntity> entities = new ArrayList<DropboxEntity>();

    private DropboxMetadataListener listener = null;
    private DateFormat dateFormat;
    private Context context;

    public DropboxMetadata(DateFormat dateFormat, Context context, DropboxMetadataListener listener)
    {
        this.dateFormat = dateFormat;
        this.listener=listener;
        this.context = context;
    }

    @Override
    protected List<DropboxEntity> doInBackground(String... params)
    {
        checkSubdirectories(params[0], params[1], params[2]);
        return entities;
    }

    @Override
    protected void onPostExecute(List<DropboxEntity> s)
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
        //Log.d(TAG, response);
        try{
            JSONObject jsonObject = new JSONObject(response);
            Boolean isDir = jsonObject.getBoolean("is_dir");
            String mimeType = null;
            if(jsonObject.has("mime_type")) mimeType = jsonObject.getString("mime_type");
            entities.add(new DropboxEntity(isDir?DropboxEntity.TYPE_FOLDER:DropboxEntity.TYPE_FILE, path, mimeType, dateFormat.parse(jsonObject.getString("modified"))));
            if(jsonObject.has("contents"))
            {
                JSONArray arr = jsonObject.getJSONArray("contents");
                for(int i = 0; i < arr.length(); i++ )
                {
                    checkSubdirectories(url, folder, arr.getJSONObject(i).getString("path"));
                }
            }

        }
        catch (JSONException | ParseException e)
        {
            Log.e(TAG, e.getMessage());
        }
    }
    protected String singlePOST(String urlString, String urlShared, String urlPath)
    {
        String resultToDisplay = "";
        try {
            urlString += "?link="+ URLEncoder.encode(urlShared, "UTF-8")+"&path="+urlPath;
            URL url = new URL(urlString);
            JSONObject jsonData = new JSONObject();
            jsonData.put("link", urlShared);
            jsonData.put("path", urlPath);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type","application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer "+context.getString(R.string.api_token));

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
                //Log.d(TAG, resultToDisplay);
            }

        } catch (Exception e) {

            System.out.println(e.getMessage());

        }

        return resultToDisplay;
    }

    public interface DropboxMetadataListener
    {
        void onTaskStart();
        void onTaskEnd(List<DropboxEntity> entities);
    }
}
