package com.roundel.fizyka;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by RouNdeL on 2016-10-11.
 */
public class TimePreference extends DialogPreference
{
    private Calendar mCalendar;
    private TimePicker mPicker = null;

    public TimePreference(Context context)
    {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mCalendar = new GregorianCalendar();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        builder.setTitle(null);
    }

    @Override
    protected View onCreateDialogView()
    {
        mPicker = new TimePicker(getContext());
        if(DateFormat.is24HourFormat(getContext())) mPicker.setIs24HourView(true);
        return (mPicker);
    }

    @Override
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);
        mPicker.setHour(mCalendar.get(Calendar.HOUR_OF_DAY));
        mPicker.setMinute(mCalendar.get(Calendar.MINUTE));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        if(positiveResult)
        {
            mCalendar.set(Calendar.HOUR_OF_DAY, mPicker.getHour());
            mCalendar.set(Calendar.MINUTE, mPicker.getMinute());

            if(callChangeListener(mCalendar.getTimeInMillis()))
            {
                persistLong(mCalendar.getTimeInMillis());
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index)
    {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
    {

        if(restoreValue)
        {
            if(defaultValue == null)
            {
                mCalendar.setTimeInMillis(getPersistedLong(System.currentTimeMillis()));
            }
            else
            {
                mCalendar.setTimeInMillis(Long.parseLong(getPersistedString((String) defaultValue)));
            }
        }
        else
        {
            if(defaultValue == null)
            {
                mCalendar.setTimeInMillis(System.currentTimeMillis());
            }
            else
            {
                mCalendar.setTimeInMillis(Long.parseLong((String) defaultValue));
            }
        }
        setSummary(getSummary());
    }
}