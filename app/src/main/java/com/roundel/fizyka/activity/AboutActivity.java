package com.roundel.fizyka.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.roundel.fizyka.Connectivity;
import com.roundel.fizyka.R;
import com.roundel.fizyka.update.UpdateChecker;
import com.roundel.fizyka.update.UpdateDownloader;

public class AboutActivity extends AppCompatActivity
{
    TextView newVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final TextView versionHolder = (TextView) findViewById(R.id.versionHolder);
        newVersion = (TextView) findViewById(R.id.newVersion);
        try
        {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionHolder.setText(getString(R.string.about_version) + " " + info.versionName);
        }
        catch(PackageManager.NameNotFoundException e)
        {
            Log.e("ABOUT", e.getMessage());
        }
        findViewById(R.id.github_mark).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rounndel/fizyka")));
            }
        });
        Connectivity.hasAccess(new Connectivity.onHasAccessResponse()
        {
            @Override
            public void onConnectionCheckStart()
            {

            }

            @Override
            public void onConnectionAvailable(Long responseTime)
            {
                final UpdateChecker manager = new UpdateChecker(new UpdateChecker.UpdateCheckerListener()
                {
                    @Override
                    public void onTaskStart()
                    {

                    }

                    @Override
                    public void onTaskEnd(String version)
                    {
                        try
                        {
                            PackageManager manager = getApplicationContext().getPackageManager();
                            PackageInfo info = manager.getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_ACTIVITIES);
                            if(checkIfNew(version, info.versionName))
                            {
                                newVersion.setText(getString(R.string.about_version_new_found) + " " + version);
                                UpdateDownloader downloader = new UpdateDownloader(version);
                                downloader.start(getApplicationContext());

                                SharedPreferences.Editor downloadPrefsEditor = getSharedPreferences("download_references", Context.MODE_PRIVATE).edit();

                                downloadPrefsEditor.putLong(UpdateDownloader.DOWNLOAD_REFERENCE, downloader.getDownloadReference());
                                downloadPrefsEditor.putString(UpdateDownloader.DOWNLOAD_VERSION, version);
                                downloadPrefsEditor.apply();
                            }
                            else
                                newVersion.setText(getString(R.string.about_version_new_not_found));
                        }
                        catch(PackageManager.NameNotFoundException e)
                        {
                        }
                    }
                });
                manager.execute();
            }

            @Override
            public void onConnectionUnavailable()
            {
                newVersion.setText(getString(R.string.about_version_new_not_found));
            }
        });
    }


    public boolean checkIfNew(String newVersion, String oldVersion)
    {
        String newVersionNum = Character.isLetter(newVersion.charAt(newVersion.length() - 1)) ? newVersion.substring(0, newVersion.length() - 1) : newVersion;
        String oldVersionNum = Character.isLetter(oldVersion.charAt(oldVersion.length() - 1)) ? oldVersion.substring(0, oldVersion.length() - 1) : oldVersion;

        return newVersionNum.compareTo(oldVersionNum) == 0 ? newVersion.compareTo(oldVersion) > 0 : newVersionNum.compareTo(oldVersionNum) > 0;
    }
}
