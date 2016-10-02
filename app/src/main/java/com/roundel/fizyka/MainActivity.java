package com.roundel.fizyka;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatPreferenceActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{
    public static String mFolderUrl;
    public static String mFolderPath;
    public static Date mRecentUpdate;
    public static Date mNewRecentDate;
    public static boolean mExtract;
    public static DateFormat mDropboxDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private DropboxDownloader mDownloader = new DropboxDownloader("", "/");

    public static String UNIX_BEGGING_DATE = "Thu, 01 Jan 1970 00:00:00 +0000";

    public int NO_UPDATE = 1;
    public int UPDATE = 0;
    public static int SWITCH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPreferenceFragment preferenceFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
        loadData(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_reset:
                DialogFragment dialog = new RestartDialogFragment();
                Bundle args = new Bundle();
                args.putString("TITLE", getResources().getString(R.string.default_title));
                args.putString("MESSAGE", getResources().getString(R.string.default_message));
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), "tag");
                return true;
            case R.id.menu_refresh:
                if(isOnline(this))
                {
                    DropboxMetadata dropboxMetadata= new DropboxMetadata(mDropboxDateFormat, new DropboxMetadata.DropboxMetadataListener()
                    {
                        @Override
                        public void onTaskEnd(String result)
                        {
                            completeRefresh(item);
                            try
                            {
                                Date date = mDropboxDateFormat.parse(result);
                                mNewRecentDate = date.after(mRecentUpdate) ? date : mRecentUpdate;
                                startDownload(date.after(mRecentUpdate));
                            }
                            catch (ParseException e)
                            {
                                if(result.equals(DropboxMetadata.ERROR_FORBIDDEN))
                                    Toast.makeText(MainActivity.this, getString(R.string.toast_error_forbidden), Toast.LENGTH_SHORT).show();
                                else if(result.equals(DropboxMetadata.ERROR_NOT_FOUND))
                                    Toast.makeText(MainActivity.this, getString(R.string.toast_error_not_found), Toast.LENGTH_SHORT).show();
                                else if(result.equals(DropboxMetadata.ERROR_CONNECTION_TIMED_OUT))
                                    Toast.makeText(MainActivity.this, getString(R.string.toast_error_connection_timed_out), Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(MainActivity.this, getString(R.string.toast_error_unknown), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onTaskStart()
                        {
                            Log.d("DATES", "Task started");
                            refresh(item);
                            Toast.makeText(MainActivity.this, getString(R.string.toast_refresh_start), Toast.LENGTH_SHORT).show();
                        }
                    });
                    dropboxMetadata.execute(getString(R.string.api_url), mFolderUrl, "/");
                }
                else
                {
                    refresh(item);
                    completeRefresh(item);
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_no_network), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_clear_date:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("date", UNIX_BEGGING_DATE);
                editor.apply();
                return true;
            case R.id.menu_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == NO_UPDATE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            startDownload(false);
        }
        else if(requestCode == UPDATE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            startDownload(true);
        }
        else if(requestCode == SWITCH  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(MainActivity.this, getString(R.string.permission_allowed_notification), Toast.LENGTH_SHORT).show();
        }
        else if(requestCode != SWITCH)
        {
            Toast.makeText(MainActivity.this, getString(R.string.permission_denied_WRITE_EXTERNAL_STOARGE), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(MainActivity.this, getString(R.string.permission_denied_notification), Toast.LENGTH_SHORT).show();
        }
    }

    private void startDownload(Boolean newVersionAvailable)
    {
        mDownloader = new DropboxDownloader(mFolderUrl.replace("?dl=0", "?dl=1"), mFolderPath);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, newVersionAvailable ? UPDATE : NO_UPDATE);
        }
        else{
            if(newVersionAvailable)
            {
                builder.setTitle(getString(R.string.update_dialog_title));
                builder.setMessage(getString(R.string.update_dialog_desc));
            }
            else
            {
                builder.setTitle(getString(R.string.no_update_dialog_title));
                builder.setMessage(getString(R.string.no_update_dialog_desc));
            }
            builder.setPositiveButton(getString(R.string.notify_button), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface p1, int p2)
                {
                    mDownloader.start(getApplicationContext());
                    saveData(mDropboxDateFormat.format(mNewRecentDate));
                }
            });
            builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface p1, int p2)
                {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null) && netInfo.isConnectedOrConnecting();
    }

    private void refresh(MenuItem refreshItem) {
     /* Attach a rotating ImageView to the refresh item as an ActionView */
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_button_layout, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotation_animation);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
    }

    private void completeRefresh(final MenuItem refreshItem) {
        if(refreshItem != null && refreshItem.getActionView() != null)
        {
            refreshItem.getActionView().getAnimation().setAnimationListener(new Animation.AnimationListener()
            {
                @Override
                public void onAnimationStart(Animation animation)
                {
                }

                @Override
                public void onAnimationEnd(Animation animation)
                {
                }

                @Override
                public void onAnimationRepeat(Animation animation)
                {
                    try
                    {
                        refreshItem.getActionView().clearAnimation();
                        refreshItem.setActionView(null);
                    }
                    catch (NullPointerException e)
                    {

                    }
                }
            });
        }

    }

    public static void loadData(Context context)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        mFolderUrl = sp.getString("download_url", "https://www.dropbox.com/sh/ya38ajmh9bezwhz/AABdJ69NcP-TDN4XlnNG83t_a?dl=0");
        mFolderPath = sp.getString("download_path", "/fizyka/");
        mExtract = sp.getBoolean("extract", true);

        if(mFolderPath.charAt(mFolderPath.length()-1) != '/')
        {
            mFolderPath+="/";
            sp.edit().putString("download_path", mFolderPath).apply();
        }
        if(mFolderPath.charAt(0) != '/')
        {
            mFolderPath = "/"+mFolderPath;
            sp.edit().putString("download_path", mFolderPath).apply();
        }
        try
        {
            mRecentUpdate = mDropboxDateFormat.parse(sp.getString("date", UNIX_BEGGING_DATE));
            mNewRecentDate = mRecentUpdate;
        }catch (ParseException e)
        {
            Log.d("DATE", e.getMessage());
        }
    }

    public void saveData(String data)
    {
        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();
        sp.putString("date", data);
        sp.apply();
        loadData(this);
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            loadData(getActivity());

            switch (key)
            {
                case "refresh_time":
                    if(prefs.getBoolean("notification", false))
                    {
                        NotificationEventReceiver.setupAlarm(getContext(), Long.parseLong(prefs.getString(key, "240")));
                    }
                    break;
                case "notification":
                    if(prefs.getBoolean("notification", false))
                    {
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SWITCH);
                            SwitchPreference preference = (SwitchPreference) findPreference("notification");
                            preference.setChecked(false);
                        }
                    }
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            if(prefs.getBoolean("notification", false))
            {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SWITCH);
                    SwitchPreference preference = (SwitchPreference) findPreference("notification");
                    preference.setChecked(false);
                }
            }
        }
    }

}