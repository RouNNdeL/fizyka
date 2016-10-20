package com.roundel.fizyka;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
public class FileAdapter extends ArrayAdapter<DropboxEntity>
{
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

    public FileAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public FileAdapter(Context context, int resource, List<DropboxEntity> items, String folder) {
        super(context, resource, items);
        mResource = resource;
        this.folderPath = folder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View fileView = convertView;

        if(fileView == null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            fileView = inflater.inflate(mResource, null);
        }

        DropboxEntity entity = getItem(position);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+folderPath+entity.getPath());
        fileView.setEnabled(file.exists());
        fileView.setClickable(!file.exists());

        ImageView imageView = (ImageView) fileView.findViewById(R.id.fileIcon);
        Drawable icon;
        TextView size = (TextView) fileView.findViewById(R.id.fileSize);
        if (entity.getType() == DropboxEntity.TYPE_FILE)
        {
            size.setText(DropboxEntity.formatSize(entity.getSize(), getContext()));
            //ImageView iconBg = (ImageView) fileView.findViewById(R.id.fileIconBg);
            //iconBg.setColorFilter(getContext().getColor(android.R.color.white), PorterDuff.Mode.CLEAR);
            imageView.setScaleX(0.9f);
            imageView.setScaleY(0.9f);
            if(MIME_TYPE_PDF.contains(entity.getMimeType()))
            {
                icon = getContext().getDrawable(R.drawable.file_pdf_white);
                if(fileView.isEnabled())
                    icon.setColorFilter(getContext().getColor(R.color.pdfRed), PorterDuff.Mode.MULTIPLY);
                else
                    icon.setColorFilter(getContext().getColor(R.color.pdfRed_disabled), PorterDuff.Mode.MULTIPLY);
            }
            else if(MIME_TYPE_IMAGE.contains(entity.getMimeType()))
            {
                icon = getContext().getDrawable(R.drawable.file_image_white);
                if(fileView.isEnabled())
                    icon.setColorFilter(getContext().getColor(R.color.pdfRed), PorterDuff.Mode.MULTIPLY);
                else
                    icon.setColorFilter(getContext().getColor(R.color.pdfRed_disabled), PorterDuff.Mode.MULTIPLY);
            }
            else if(MIME_TYPE_TXT.contains(entity.getMimeType()))
            {
                icon = getContext().getDrawable(R.drawable.file_document_text);
                if(fileView.isEnabled())
                    icon.setColorFilter(getContext().getColor(R.color.textBlue), PorterDuff.Mode.MULTIPLY);
                else
                    icon.setColorFilter(getContext().getColor(R.color.textBlue_disabled), PorterDuff.Mode.MULTIPLY);
            }
            else
            {
                icon = getContext().getDrawable(R.drawable.file_default_white_24dp);
                if(fileView.isEnabled())
                    icon.setColorFilter(getContext().getColor(R.color.textBlue), PorterDuff.Mode.MULTIPLY);
                else
                    icon.setColorFilter(getContext().getColor(R.color.textBlue_disabled), PorterDuff.Mode.MULTIPLY);
            }
        }
        else
        {
            if(entity.getSize() == 1)
                size.setText(String.format(
                Locale.getDefault(),
                getContext().getString(R.string.file_item),
                entity.getSize()));
            else
                size.setText(String.format(
                        Locale.getDefault(),
                        getContext().getString(R.string.file_item_plural),
                        entity.getSize()));
            icon = getContext().getDrawable(R.drawable.file_folder_white_24dp);

            if(fileView.isEnabled())
                icon.setColorFilter(getContext().getColor(R.color.folderGrey), PorterDuff.Mode.MULTIPLY);
            else
                icon.setColorFilter(getContext().getColor(R.color.folderGrey_disabled), PorterDuff.Mode.MULTIPLY);
        }
        imageView.setImageDrawable(icon);

        TextView title = (TextView) fileView.findViewById(R.id.fileText);
        title.setText(entity.getName());
        if(fileView.isEnabled())
            title.setTextColor(getContext().getColor(R.color.primaryText));
        else
            title.setTextColor(getContext().getColor(R.color.primaryText_disabled));

        TextView date = (TextView) fileView.findViewById(R.id.fileDate);
        date.setText(dateFormat.format(entity.getDate()));
        if(fileView.isEnabled())
            date.setTextColor(getContext().getColor(R.color.secondaryText));
        else
            date.setTextColor(getContext().getColor(R.color.secondaryText_disabled));

        if(!fileView.isEnabled())
        {
            TextView fileMissing = (TextView) fileView.findViewById(R.id.fileMissing);
            fileMissing.setText(getContext().getString(R.string.file_missing));
            fileMissing.setTextColor(getContext().getColor(R.color.primaryText_disabled));
        }

        if(fileView.isEnabled())
            size.setTextColor(getContext().getColor(R.color.secondaryText));
        else
            size.setTextColor(getContext().getColor(R.color.secondaryText_disabled));


        return fileView;
    }
}
