package com.roundel.fizyka.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
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

import com.roundel.fizyka.Connectivity;
import com.roundel.fizyka.FileAdapter;
import com.roundel.fizyka.R;
import com.roundel.fizyka.RadioDialog;
import com.roundel.fizyka.dropbox.DropboxEntity;
import com.roundel.fizyka.update.UpdateChecker;
import com.roundel.fizyka.update.UpdateDownloader;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by RouNdeL on 2016-10-16.
 */
public class FileExplorerActivity extends AppCompatActivity
{
    private String currentPath = "/";
    private String backPath = "/";
    private List<DropboxEntity> mEntities = new ArrayList<>();

    private ListView mListView;
    private LinearLayout mPathsContainer;
    private int sortingMode;
    private String folderPath;
    private List<Integer> sortingFlags = new ArrayList<>();

    private View rootView;
    private int checkedSortingItemId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_activity);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        folderPath = preferences.getString("download_path", "");
        sortingMode = preferences.getInt("sorting_mode", DropboxEntity.SORT_NAME);
        List<String> sortingFlagsString = Arrays.asList(preferences.getString("sorting_flags", "").split(";"));

        for(String s : sortingFlagsString){
            try
            {
                sortingFlags.add(Integer.parseInt(s));
            }
            catch (NumberFormatException e)
            {
                break;
            }
        }

        rootView = findViewById(R.id.coordinatorLayoutFileExplorer);

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
                    public void onTaskEnd(final String version)
                    {
                        try
                        {
                            PackageManager manager = getApplicationContext().getPackageManager();
                            PackageInfo info = manager.getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_ACTIVITIES);
                            if(version != null && UpdateDownloader.checkIfNew(version, info.versionName))
                            {
                                if(rootView != null)
                                {
                                    Snackbar snackbar = Snackbar
                                            .make(rootView, getString(R.string.update_title), 5000)
                                            .setAction(getString(R.string.download_notify_button), new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View view)
                                                {
                                                    UpdateDownloader downloader = new UpdateDownloader(version);
                                                    downloader.start(getApplicationContext());

                                                    SharedPreferences.Editor downloadPrefsEditor = getSharedPreferences("download_references", Context.MODE_PRIVATE).edit();
                                                    downloadPrefsEditor.putLong(UpdateDownloader.DOWNLOAD_REFERENCE, downloader.getDownloadReference());
                                                    downloadPrefsEditor.putString(UpdateDownloader.DOWNLOAD_VERSION, version);
                                                    downloadPrefsEditor.apply();
                                                }
                                            });

                                    snackbar.show();
                                }
                                else
                                {
                                    Log.d("FileExplorer", "Coordinator is null");
                                }
                            }
                        }
                        catch (PackageManager.NameNotFoundException e)
                        {
                        }
                    }
                });
                manager.execute();
            }

            @Override
            public void onConnectionUnavailable()
            {

            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        folderPath = preferences.getString("download_path", "");

        mListView = (ListView) findViewById(R.id.fileListView);
        mPathsContainer = (LinearLayout) findViewById(R.id.currentPathContainer);

        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir()+"/dropbox_entities.dat"));
            mEntities = (List<DropboxEntity>) ois.readObject();

            if (!DropboxEntity.filesExist(mEntities, Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath))
            {
                Toast.makeText(FileExplorerActivity.this, getString(R.string.missing_files), Toast.LENGTH_LONG).show();
            }

            updateListView(mListView, mEntities, currentPath, sortingMode, sortingFlags, false);
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
                        updateListView(mListView, mEntities, currentPath, sortingMode, sortingFlags, false);
                        updatePathContainer(mPathsContainer, currentPath);
                    }
                    else if(clickedItem.getType() == DropboxEntity.TYPE_FILE)
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
            textView.setText(getString(R.string.class_changed_update));
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
            case R.id.menu_sort:
                onShowSortDialog();
                return true;

            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_about:
                Intent intent1 = new Intent(this, AboutActivity.class);
                startActivity(intent1);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed()
    {
        if(Objects.equals(currentPath, "/")) super.onBackPressed();
        else
        {
            updateListView(mListView, mEntities, backPath, sortingMode, sortingFlags, false);
            updatePathContainer(mPathsContainer, backPath);
            currentPath = backPath;
            backPath = DropboxEntity.getParentDirectoryFromString(currentPath);
        }
    }

    private void updateListView(final ListView listView, List<DropboxEntity> entities, String path, final int sortingMode, List<Integer> flags, boolean animateArrow)
    {
        List<DropboxEntity> entitiesFromPath = new ArrayList<>();
        for (DropboxEntity entity : entities)
        {
            if(Objects.equals(entity.getParentDirectory(), path))
            {
                entitiesFromPath.add(entity);
            }
        }
        FileAdapter adapter = new FileAdapter(this,
                R.layout.file_row, folderPath,
                !sortingFlags.contains(DropboxEntity.FLAG_INVERSE),
                animateArrow,
                sortingMode,
                new FileAdapter.OnSortArrowClickListener()
                {
                    @Override
                    public void onClick(View view) {
                        if(sortingFlags.contains(DropboxEntity.FLAG_INVERSE))
                        {
                            sortingFlags.remove((Integer) DropboxEntity.FLAG_INVERSE);
                        }
                        else
                        {
                            sortingFlags.add(DropboxEntity.FLAG_INVERSE);
                        }
                        updateListView(listView, mEntities, currentPath, sortingMode, sortingFlags, true);
                    }
        });

        List<DropboxEntity> folders = DropboxEntity.getEntitiesByType(entitiesFromPath, DropboxEntity.TYPE_FOLDER);
        List<DropboxEntity> files = DropboxEntity.getEntitiesByType(entitiesFromPath, DropboxEntity.TYPE_FILE);

        folders = DropboxEntity.sort(folders, sortingMode, flags);
        files = DropboxEntity.sort(files, sortingMode, flags);

        adapter.addFolders(folders);
        adapter.addFiles(files);
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
                updateListView(mListView, mEntities, path, sortingMode, sortingFlags, false);
                updatePathContainer(layout, path);
                currentPath = path;
                backPath = DropboxEntity.getParentDirectoryFromString(currentPath);
            }
        });
        layout.addView(child, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return child;
    }

    private void onShowSortDialog()
    {
        List<Integer> sortingModes = new ArrayList<>(Arrays.asList(new Integer[]{
                DropboxEntity.SORT_NAME,
                DropboxEntity.SORT_DATE,
                DropboxEntity.SORT_SIZE
        }));
        final RadioDialog dialog = new RadioDialog();
        dialog.setTitle(getString(R.string.sort_title));
        dialog.setContent(sortingModes);
        dialog.setChecked(sortingMode);
        dialog.addOnItemClickListener(new RadioDialog.OnItemClickListener() {
            @Override
            public void onClick(int mode) {
                sortingMode = mode;
                dialog.dismiss();
                updateListView(mListView, mEntities, currentPath, sortingMode, sortingFlags, false);
            }
        });
        dialog.show(getSupportFragmentManager(), "SortChoosingDialog");
    }
}
