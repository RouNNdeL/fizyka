package com.roundel.fizyka;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        final TextView versionHolder = (TextView) findViewById(R.id.versionHolder);
        try
        {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), PackageManager.GET_ACTIVITIES);
            versionHolder.setText(getString(R.string.about_version)+" "+info.versionName);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.e("ABOUT", e.getMessage());
        }
    }
}
