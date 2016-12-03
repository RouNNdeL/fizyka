package com.roundel.fizyka;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.roundel.fizyka.dropbox.DropboxEntity;

import java.util.List;
import java.util.Objects;

/**
 * Created by Krzysiek on 2016-10-25.
 */

public class RadioDialog extends AppCompatDialogFragment
{
    List<Integer> mItemList;
    OnItemClickListener mListener;
    String mTitle;
    int mCheckedItem;

    int textSize = 18;
    int paddingTop = 24;
    int paddingBottom = 24;
    int paddingLeft = 24;
    int paddingRight = 24;

    public void setTitle(String title)
    {
        this.mTitle = title;
    }

    public void addOnItemClickListener(OnItemClickListener listener)
    {
        this.mListener = listener;
    }

    public void setContent(List<Integer> list)
    {
        this.mItemList = list;
    }

    public void setChecked(int item)
    {
        this.mCheckedItem = item;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        /*Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.radio_dialog);
        dialog.setTitle("Sort by");

        RadioGroup group = (RadioGroup) dialog.findViewById(R.id.radio_group);

        for(final String title: mItemList)
        {
            RadioButton button = new RadioButton(getActivity());
            button.setText(title);
            group.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onClick(title);
                }
            });
        }*/

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View radioLayout = inflater.inflate(R.layout.radio_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle)
                .setView(radioLayout);

        AlertDialog dialog = builder.create();

        RadioGroup group = (RadioGroup) radioLayout.findViewById(R.id.radio_group);

        for(final int mode : mItemList)
        {
            RadioButton button = new RadioButton(getActivity());
            button.setText(DropboxEntity.getLocalizedSortingName(getContext(), mode));
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            button.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    mListener.onClick(mode);
                }
            });
            if(Objects.equals(mode, mCheckedItem))
                button.setChecked(true);
            group.addView(button);
        }


        return dialog;
    }

    public interface OnItemClickListener
    {
        void onClick(int mode);
    }
}
