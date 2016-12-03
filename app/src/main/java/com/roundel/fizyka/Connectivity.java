package com.roundel.fizyka;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by RouNdeL on 2016-10-10.
 */


public class Connectivity
{
    public static NetworkInfo getNetworkInfo(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnectedWifi(Context context)
    {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isConnectedCellular(Context context)
    {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isConnectedEthernet(Context context)
    {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_ETHERNET);
    }

    public static boolean isConnected(Context context)
    {
        NetworkInfo info = Connectivity.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static void hasAccess(final onHasAccessResponse listener)
    {
        CheckConnection check = new CheckConnection(new TaskListener()
        {
            @Override
            public void onTaskStart()
            {
                listener.onConnectionCheckStart();
            }

            @Override
            public void onTaskEnd(long l)
            {
                if(l > -1) listener.onConnectionAvailable(l);
                else listener.onConnectionUnavailable();
            }
        });
        check.execute();
    }

    private static class CheckConnection extends AsyncTask<String, Integer, Long>
    {
        TaskListener mListener;
        Long timeStart;

        public CheckConnection(TaskListener listener)
        {
            this.mListener = listener;
        }

        @Override
        protected void onPreExecute()
        {
            timeStart = System.currentTimeMillis();
            mListener.onTaskStart();
        }


        @Override
        protected Long doInBackground(String... params)
        {
            try
            {
                URL url = new URL("https://gstatic.com/generate_204");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int response = connection.getResponseCode();
                if(response == HttpURLConnection.HTTP_NO_CONTENT)
                    return System.currentTimeMillis() - timeStart;
                else return Long.valueOf(-1);
            }
            catch(IOException e)
            {
                return Long.valueOf(-1);
            }
        }

        @Override
        protected void onPostExecute(Long result)
        {
            mListener.onTaskEnd(result);
        }
    }

    private interface TaskListener
    {
        void onTaskStart();

        void onTaskEnd(long b);
    }

    public interface onHasAccessResponse
    {
        void onConnectionCheckStart();

        void onConnectionAvailable(Long responseTime);

        void onConnectionUnavailable();
    }
}