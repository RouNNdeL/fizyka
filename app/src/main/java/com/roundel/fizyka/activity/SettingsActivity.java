package com.roundel.fizyka.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.roundel.fizyka.Connectivity;
import com.roundel.fizyka.R;
import com.roundel.fizyka.RestartDialogFragment;
import com.roundel.fizyka.dropbox.DropboxDownloader;
import com.roundel.fizyka.dropbox.DropboxEntity;
import com.roundel.fizyka.dropbox.DropboxLinkValidator;
import com.roundel.fizyka.dropbox.DropboxMetadata;
import com.roundel.fizyka.dropbox.NotificationEventReceiver;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity implements ActivityCompat.OnRequestPermissionsResultCallback
{
    public static String mFolderUrl;
    public static String mFolderPath;
    public static Date mRecentUpdate;
    public static Date mNewRecentDate;
    public static boolean mExtract;
    public static final DateFormat mDropboxDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public static String UNIX_BEGGING_DATE = "Thu, 01 Jan 1970 00:00:00 +0000";
    public static final String PREFERENCE_FRAGMENT_ID = "com.roundel.fizyka.FRAGMENT";

    public int NO_UPDATE = 1;
    public int UPDATE = 0;
    public static int SWITCH = 2;

    private List<DropboxEntity> entitiesToSave;
    private List<DropboxEntity> deltaEntities;

    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MyPreferenceFragment preferenceFragment = new MyPreferenceFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, preferenceFragment)
                /*.addToBackStack(PREFERENCE_FRAGMENT_ID)*/
                .commit();
        loadData(this);
        rootView = findViewById(android.R.id.content);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item)
    {
        switch(item.getItemId())
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
                checkForUpdates(item);
                return true;

            case R.id.menu_clear_date:
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("date", UNIX_BEGGING_DATE);
                editor.apply();
                return true;

            case R.id.menu_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;

            case android.R.id.home:
                super.onBackPressed();
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == NO_UPDATE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            showDownloadDialog(false);
        }
        else if(requestCode == UPDATE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            showDownloadDialog(true);
        }
        else if(requestCode == SWITCH && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(SettingsActivity.this, getString(R.string.permission_allowed_notification), Toast.LENGTH_SHORT).show();
        }
        else if(requestCode != SWITCH)
        {
            Toast.makeText(SettingsActivity.this, getString(R.string.permission_denied_WRITE_EXTERNAL_STOARGE), Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(SettingsActivity.this, getString(R.string.permission_denied_notification), Toast.LENGTH_SHORT).show();
        }
    }


    private void checkForUpdates(final MenuItem refreshItem)
    {
        Connectivity.hasAccess(new Connectivity.onHasAccessResponse()
        {
            @Override
            public void onConnectionCheckStart()
            {
                refresh(refreshItem);
            }

            @Override
            public void onConnectionAvailable(final Long responseTime)
            {
                final DropboxMetadata dropboxMetadata = new DropboxMetadata(mDropboxDateFormat, getApplicationContext(), new DropboxMetadata.DropboxMetadataListener()
                {
                    @Override
                    public void onTaskStart()
                    {
                        Toast.makeText(SettingsActivity.this, getString(R.string.toast_refresh_start), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTaskEnd(List<DropboxEntity> result)
                    {
                        completeRefresh(refreshItem);
                        //Log.d("DropboxEntity", DropboxEntity.listToString(result));
                        try
                        {
                            File file = new File(getFilesDir() + "/dropbox_entities.dat");
                            if(!file.exists()) file.createNewFile();

                            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir() + "/dropbox_entities.dat"));
                            List<DropboxEntity> oldEntities = (List<DropboxEntity>) ois.readObject();

                            List<DropboxEntity> newEntities = DropboxEntity.getNewEntities(oldEntities, result);

                            deltaEntities = newEntities;

                            if(newEntities.size() > 0)
                            {
                                entitiesToSave = result;
                                showDownloadDialog(true);
                            }
                            else
                            {
                                entitiesToSave = null;
                                showDownloadDialog(false);
                            }
                        }
                        catch(InvalidClassException | ClassNotFoundException | EOFException e)
                        {
                            try
                            {
                                File file = new File(getFilesDir() + "/dropbox_entities.dat");
                                if(!file.exists()) file.createNewFile();
                                entitiesToSave = result;
                                deltaEntities = result;
                                showDownloadDialog(true);

                            }
                            catch(IOException e1)
                            {
                                Log.e("ReadError1", "", e1);
                            }
                        }
                        catch(IOException e2)
                        {
                            Log.e("ReadError1", "", e2);
                        }
                    }
                });
                DropboxLinkValidator validator = new DropboxLinkValidator(getApplicationContext(), new DropboxLinkValidator.DropboxLinkValidatorListener()
                {
                    @Override
                    public void onTaskStart()
                    {

                    }

                    @Override
                    public void onTaskEnd(String result)
                    {
                        switch(result)
                        {
                            case DropboxLinkValidator.NO_ERROR:
                                dropboxMetadata.execute(getString(R.string.api_url), mFolderUrl, "/");
                                break;
                            case DropboxLinkValidator.ERROR_FORBIDDEN:
                                Toast.makeText(SettingsActivity.this, getString(R.string.toast_error_forbidden), Toast.LENGTH_SHORT).show();
                                completeRefresh(refreshItem);
                                break;
                            case DropboxLinkValidator.ERROR_NOT_FOUND:
                                Toast.makeText(SettingsActivity.this, getString(R.string.toast_error_not_found), Toast.LENGTH_SHORT).show();
                                completeRefresh(refreshItem);
                                break;
                            case DropboxLinkValidator.ERROR_CONNECTION_TIMED_OUT:
                                Toast.makeText(SettingsActivity.this, getString(R.string.toast_error_connection_timed_out), Toast.LENGTH_SHORT).show();
                                completeRefresh(refreshItem);
                                break;
                            default:
                                Toast.makeText(SettingsActivity.this, getString(R.string.toast_error_unknown), Toast.LENGTH_SHORT).show();
                                completeRefresh(refreshItem);
                                break;
                        }
                    }
                });
                validator.execute(getString(R.string.api_url), mFolderUrl);
            }

            @Override
            public void onConnectionUnavailable()
            {
                completeRefresh(refreshItem);
                //Toast.makeText(SettingsActivity.this, getResources().getString(R.string.toast_no_network), Toast.LENGTH_SHORT).show();
                if(rootView != null)
                {
                    Snackbar snackbar = Snackbar
                            .make(rootView, getString(R.string.toast_no_network), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.snackbar_button_retry), new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    checkForUpdates(refreshItem);
                                }
                            });

                    snackbar.show();
                }
                else
                {
                    Log.d("SettingsActivity", "Coordinator is null");
                }
            }
        });
    }

    private void showDownloadDialog(Boolean newVersionAvailable)
    {
        final DropboxDownloader downloader = new DropboxDownloader(mFolderUrl, mFolderPath);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, newVersionAvailable ? UPDATE : NO_UPDATE);
        }
        else
        {
            if(newVersionAvailable)
            {
                builder.setTitle(getString(R.string.update_dialog_title));
                builder.setMessage(getString(R.string.update_dialog_desc) + "\n" + DropboxEntity.getChangelog(deltaEntities, this));
            }
            else
            {
                builder.setTitle(getString(R.string.no_update_dialog_title));
                builder.setMessage(getString(R.string.no_update_dialog_desc));
            }
            builder.setPositiveButton(getString(R.string.download_notify_button), new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface p1, int p2)
                {
                    downloader.start(getApplicationContext());
                    /*IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                    DropboxDownloadCompletedBroadcastReceiver receiver = new DropboxDownloadCompletedBroadcastReceiver();
                    registerReceiver(receiver, filter);*/
                    SharedPreferences.Editor downloadPrefsEditor = getSharedPreferences("download_references", Context.MODE_PRIVATE).edit();

                    downloadPrefsEditor.putLong(DropboxDownloader.DOWNLOAD_REFERENCE, downloader.getDownloadReference());
                    downloadPrefsEditor.apply();
                    saveData(mDropboxDateFormat.format(mNewRecentDate));
                    saveEntities();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface p1, int p2)
                {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void refresh(MenuItem refreshItem)
    {
     /* Attach a rotating ImageView to the refresh item as an ActionView */
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_button_layout, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_rotation_animation);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
    }

    private void completeRefresh(final MenuItem refreshItem)
    {
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
                    catch(NullPointerException e)
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

        if(mFolderPath.charAt(mFolderPath.length() - 1) != '/')
        {
            mFolderPath += "/";
            sp.edit().putString("download_path", mFolderPath).apply();
        }
        if(mFolderPath.charAt(0) != '/')
        {
            mFolderPath = "/" + mFolderPath;
            sp.edit().putString("download_path", mFolderPath).apply();
        }
        try
        {
            mRecentUpdate = mDropboxDateFormat.parse(sp.getString("date", UNIX_BEGGING_DATE));
            mNewRecentDate = mRecentUpdate;
        }
        catch(ParseException e)
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

    private void saveEntities()
    {
        if(entitiesToSave == null) return;
        try
        {
            FileOutputStream out = new FileOutputStream(getFilesDir() + "/dropbox_entities.dat");
            ObjectOutputStream oout = new ObjectOutputStream(out);
            oout.writeObject(entitiesToSave);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
        {
            loadData(getActivity());

            switch(key)
            {
                case "time":
                case "refresh_time":
                    if(prefs.getBoolean("notification", false))
                    {
                        NotificationEventReceiver.setupAlarm(getContext(), Long.parseLong(prefs.getString("refresh_time", "240")));
                    }
                    break;
                case "notification":
                    if(prefs.getBoolean("notification", false))
                    {
                        NotificationEventReceiver.setupAlarm(getContext(), Long.parseLong(prefs.getString("refresh_time", "240")));
                        findPreference("time").setEnabled(true);
                        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SWITCH);
                            SwitchPreference preference = (SwitchPreference) findPreference("notification");
                            preference.setChecked(false);
                        }
                    }
                    else findPreference("time").setEnabled(false);
                    break;
                /*case "extract":
                    if(prefs.getBoolean("extract", true)) findPreference("original_dates").setEnabled(true);
                    else findPreference("original_dates").setEnabled(false);*/
            }
        }

        @Override
        public void onResume()
        {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause()
        {
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
                findPreference("time").setEnabled(true);
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SWITCH);
                    SwitchPreference preference = (SwitchPreference) findPreference("notification");
                    preference.setChecked(false);
                }
            }
            else
            {
                findPreference("time").setEnabled(false);
            }
            /*if(prefs.getBoolean("extract", true)) findPreference("original_dates").setEnabled(true);
            else findPreference("original_dates").setEnabled(false);*/
        }
    }

}