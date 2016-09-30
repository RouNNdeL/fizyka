package com.roundel.fizyka;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by RouNdeL on 2016-09-30.
 */
public class MyEditTextPreference extends EditTextPreference
{
    public MyEditTextPreference(Context context) {
        super(context);
        //Auto-generated constructor stub
    }

    public MyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Auto-generated constructor stub
    }

    public MyEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //Auto-generated constructor stub
    }

    public void show()
    {
        showDialog(null);
    }
}
