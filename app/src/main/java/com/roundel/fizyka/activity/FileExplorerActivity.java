package com.roundel.fizyka.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.roundel.fizyka.FileAdapter;
import com.roundel.fizyka.R;
import com.roundel.fizyka.dropbox.DropboxEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Created by RouNdeL on 2016-10-16.
 */
public class FileExplorerActivity extends AppCompatActivity
{
    private String folderPath;
    private static String currentPath = "/";
    private static String backPath = "/";
    private static List<DropboxEntity> mEntities = new ArrayList<DropboxEntity>();
    private static ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_activity);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        folderPath = preferences.getString("download_path", "");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir()+"/dropbox_entities.dat"));
            mEntities = (List<DropboxEntity>) ois.readObject();

            mListView = (ListView) findViewById(R.id.fileListView);

            updateListView(mListView, mEntities, currentPath);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    DropboxEntity clickedItem = ((DropboxEntity) mListView.getItemAtPosition(i));
                    Log.d("ListView", "Clicked: "+clickedItem.toString());
                    if(clickedItem.getType() == DropboxEntity.TYPE_FOLDER)
                    {
                        currentPath = clickedItem.getPath()+"/";
                        backPath = clickedItem.getParentDirectory();
                        String test = clickedItem.getPath();
                        updateListView(mListView, mEntities, currentPath);
                    }
                    else
                    {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+folderPath+clickedItem.getPath());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setType(clickedItem.getMimeType());
                        intent.setData(Uri.fromFile(file));
                        startActivity(intent);
                    }
                }
            });


        } catch (NullPointerException | IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_explorer_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_about:
                Intent intent1 = new Intent(this, AboutActivity.class);
                startActivity(intent1);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        if(Objects.equals(currentPath, "/")) super.onBackPressed();
        else
        {
            updateListView(mListView, mEntities, backPath);
            currentPath = backPath;
        }
    }

    private void updateListView(ListView listView, List<DropboxEntity> entities, String path)
    {
        List<DropboxEntity> entitiesFromPath = new ArrayList<DropboxEntity>();
        for (DropboxEntity entity : entities)
        {
            if(Objects.equals(entity.getParentDirectory(), path))
            {
                entitiesFromPath.add(entity);
            }
        }
        FileAdapter adapter = new FileAdapter(this, R.layout.file_row, entitiesFromPath);
        listView.setAdapter(adapter);
    }
}
