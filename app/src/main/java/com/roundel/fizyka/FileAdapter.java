package com.roundel.fizyka;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.roundel.fizyka.dropbox.DropboxEntity;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by RouNdeL on 2016-10-16.
 */
public class FileAdapter extends BaseAdapter
{
    private List<DropboxEntity> mList = new ArrayList<>();
    private Context mContext;

    private List<String> MIME_TYPE_PDF = new ArrayList<String>(Arrays.asList(new String[]{
            "application/pdf",
            "application/x-pdf"
    }));
    private List<String> MIME_TYPE_TXT = new ArrayList<String>(Arrays.asList(new String[]{
            "application/octet-stream",
            "text/plain"
    }));
    private List<String> MIME_TYPE_IMAGE = new ArrayList<String>(Arrays.asList(new String[]{
            "image/jpg",
            "image/jpeg",
            "image/*",
            "image/bmp",
            "image/png",
            "image/gif",
            "image/x-icon",
            "image/x-rgb"
    }));

    private int mResource;
    private String folderPath;
    private DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

    public FileAdapter(Context context, int resource, String folder) {
        this.mContext = context;
        this.mResource = resource;
        this.folderPath = folder;
    }

    public void addFiles(List<DropboxEntity> entities)
    {
        mList.addAll(entities);
    }

    public void addFolders(List<DropboxEntity> entities)
    {
        mList.addAll(entities);
    }

    @Override
    public DropboxEntity getItem(int position)
    {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getCount()
    {
        return mList.size();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View fileView = convertView;

        if(fileView == null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fileView = inflater.inflate(mResource, null);
        }

        DropboxEntity entity = getItem(position);


        if (entity != null)
        {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folderPath + entity.getPath());
            fileView.setEnabled(file.exists());

            ImageView imageView = (ImageView) fileView.findViewById(R.id.fileIcon);
            Drawable icon;
            TextView size = (TextView) fileView.findViewById(R.id.fileSize);
            if (entity.getType() == DropboxEntity.TYPE_FILE)
            {
                size.setText(DropboxEntity.formatSize(entity.getSize(), mContext));
                //ImageView iconBg = (ImageView) fileView.findViewById(R.id.fileIconBg);
                //iconBg.setColorFilter(mContext.getColor(android.R.color.white), PorterDuff.Mode.CLEAR);
                imageView.setScaleX(0.9f);
                imageView.setScaleY(0.9f);

                if (!fileView.isEnabled())
                {
                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    params.weight = 2.5f;

                    TextView fileMissing = (TextView) fileView.findViewById(R.id.fileMissing);
                    fileMissing.setText(mContext.getString(R.string.file_missing));
                    fileMissing.setTextColor(mContext.getColor(R.color.primaryText_disabled));
                    fileMissing.setLayoutParams(params);

                    fileView.setClickable(!file.exists());
                }

                if (MIME_TYPE_PDF.contains(entity.getMimeType()))
                {
                    icon = mContext.getDrawable(R.drawable.file_pdf_white);
                    if (fileView.isEnabled())
                        icon.setColorFilter(mContext.getColor(R.color.pdfRed), PorterDuff.Mode.MULTIPLY);
                    else
                        icon.setColorFilter(mContext.getColor(R.color.pdfRed_disabled), PorterDuff.Mode.MULTIPLY);
                } else if (MIME_TYPE_IMAGE.contains(entity.getMimeType()))
                {
                    icon = mContext.getDrawable(R.drawable.file_image_white);
                    if (fileView.isEnabled())
                        icon.setColorFilter(mContext.getColor(R.color.pdfRed), PorterDuff.Mode.MULTIPLY);
                    else
                        icon.setColorFilter(mContext.getColor(R.color.pdfRed_disabled), PorterDuff.Mode.MULTIPLY);
                } else if (MIME_TYPE_TXT.contains(entity.getMimeType()))
                {
                    icon = mContext.getDrawable(R.drawable.file_document_text);
                    if (fileView.isEnabled())
                        icon.setColorFilter(mContext.getColor(R.color.textBlue), PorterDuff.Mode.MULTIPLY);
                    else
                        icon.setColorFilter(mContext.getColor(R.color.textBlue_disabled), PorterDuff.Mode.MULTIPLY);
                } else
                {
                    icon = mContext.getDrawable(R.drawable.file_default_white_24dp);
                    if (fileView.isEnabled())
                        icon.setColorFilter(mContext.getColor(R.color.textBlue), PorterDuff.Mode.MULTIPLY);
                    else
                        icon.setColorFilter(mContext.getColor(R.color.textBlue_disabled), PorterDuff.Mode.MULTIPLY);
                }
            } else
            {
                if (!fileView.isEnabled())
                {
                    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                    params.weight = 2f;
                    TextView fileMissing = (TextView) fileView.findViewById(R.id.fileMissing);
                    fileMissing.setText(mContext.getString(R.string.folder_missing));
                    fileMissing.setTextColor(mContext.getColor(R.color.primaryText_disabled));
                    fileMissing.setLayoutParams(params);
                }
                if (entity.getSize() == 1)
                    size.setText(String.format(
                            Locale.getDefault(),
                            mContext.getString(R.string.file_item),
                            entity.getSize()));
                else
                    size.setText(String.format(
                            Locale.getDefault(),
                            mContext.getString(R.string.file_item_plural),
                            entity.getSize()));
                icon = mContext.getDrawable(R.drawable.file_folder_white_24dp);

                if (fileView.isEnabled())
                    icon.setColorFilter(mContext.getColor(R.color.folderGrey), PorterDuff.Mode.MULTIPLY);
                else
                    icon.setColorFilter(mContext.getColor(R.color.folderGrey_disabled), PorterDuff.Mode.MULTIPLY);
            }
            imageView.setImageDrawable(icon);

            TextView title = (TextView) fileView.findViewById(R.id.fileText);
            title.setText(entity.getName());
            if (fileView.isEnabled())
                title.setTextColor(mContext.getColor(R.color.primaryText));
            else
                title.setTextColor(mContext.getColor(R.color.primaryText_disabled));

            TextView date = (TextView) fileView.findViewById(R.id.fileDate);
            date.setText(dateFormat.format(entity.getDate()));
            if (fileView.isEnabled())
                date.setTextColor(mContext.getColor(R.color.secondaryText));
            else
                date.setTextColor(mContext.getColor(R.color.secondaryText_disabled));



            if (fileView.isEnabled())
                size.setTextColor(mContext.getColor(R.color.secondaryText));
            else
                size.setTextColor(mContext.getColor(R.color.secondaryText_disabled));
        }


        return fileView;
    }
}
