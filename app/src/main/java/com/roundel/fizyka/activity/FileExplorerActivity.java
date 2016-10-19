package com.roundel.fizyka.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.roundel.fizyka.FileAdapter;
import com.roundel.fizyka.R;
import com.roundel.fizyka.dropbox.DropboxEntity;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private static LinearLayout mPathsContainer;

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        folderPath = preferences.getString("download_path", "");
        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir()+"/dropbox_entities.dat"));
            mEntities = (List<DropboxEntity>) ois.readObject();

            mListView = (ListView) findViewById(R.id.fileListView);
            mPathsContainer = (LinearLayout) findViewById(R.id.currentPathContainer);

            updateListView(mListView, mEntities, currentPath);
            updatePathContainer(mPathsContainer, currentPath);

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
                        updatePathContainer(mPathsContainer, currentPath);
                    }
                    else
                    {
                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+folderPath+clickedItem.getPath());
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), clickedItem.getMimeType());
                        PackageManager packageManager = getPackageManager();
                        List activities = packageManager.queryIntentActivities(intent,
                                PackageManager.MATCH_DEFAULT_ONLY);
                        boolean isIntentSafe = activities.size() > 0;
                        if (isIntentSafe)
                        {
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(FileExplorerActivity.this, getString(R.string.file_no_app), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


        } catch (InvalidClassException|  ClassNotFoundException | EOFException e)
        {
            TextView textView = (TextView) findViewById(R.id.updateRequiredTextView);
            textView.setVisibility(View.VISIBLE);
            textView.setText("Update required");
            findViewById(R.id.fileListView).setVisibility(View.GONE);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
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
            updatePathContainer(mPathsContainer, backPath);
            currentPath = backPath;
            backPath = DropboxEntity.getParentDirectoryFromString(currentPath);
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

    private void updatePathContainer(LinearLayout container, String path)
    {
        View current;
        String totalPath = "/";
        container.removeAllViews();
        current = addChild(container, "/", totalPath);
        for (String folder: path.split("/"))
        {
            if(!folder.isEmpty())
            {
                totalPath += folder+"/";
                current = addChild(container, folder, totalPath);
            }
        }
        TextView textView = (TextView) current.findViewById(R.id.file_path_textview);
        ImageView imageView = (ImageView) current.findViewById(R.id.file_path_arrow);
        textView.setTextColor(getColor(R.color.white));
        imageView.setVisibility(View.GONE);
    }
    private View addChild(final LinearLayout layout, String name, String path)
    {
        LinearLayout child = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.file_path_part, null);
        TextView textView = (TextView) child.findViewById(R.id.file_path_textview);
        textView.setText(name);
        textView.setTextColor(getColor(R.color.white_text_secondary));
        child.setTag(R.id.path_key, path);
        child.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String path = view.getTag(R.id.path_key).toString();
                updateListView(mListView, mEntities, path);
                updatePathContainer(layout, path);
            }
        });
        layout.addView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return child;
    }
}
