package com.roundel.fizyka;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
        findViewById(R.id.github_mark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://github.com/rounndel/fizyka")));
            }
        });
    }

}
