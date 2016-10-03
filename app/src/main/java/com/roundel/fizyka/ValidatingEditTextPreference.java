package com.roundel.fizyka;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.support.design.widget.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by RouNdeL on 2016-09-30.
 */
public class ValidatingEditTextPreference extends EditTextPreference
{
    private String TAG = "VALIDATOR";
    private EditText mEditText;
    private int mValidationType;
    private final int VALIDATION_PATH = 0;
    private final int VALIDATION_URL = 1;
    public ValidatingEditTextPreference(Context context) {
        super(context);
        //Auto-generated constructor stub
    }

    public ValidatingEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ValidatingEditTextPreference, 0, 0);

        try {
            mValidationType = a.getInteger(R.styleable.ValidatingEditTextPreference_type, VALIDATION_PATH);
        } finally {
            a.recycle();
        }
    }

    public ValidatingEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //Auto-generated constructor stub
    }


    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText)
    {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.text_input_layout);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    public void show()
    {
        showDialog(null);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        Log.d(TAG, "Works");
        if (super.getDialog() instanceof AlertDialog) {
            Log.d(TAG, "Works better");
            final AlertDialog theDialog = (AlertDialog) super.getDialog();

            Button b = theDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            ValidatingOnClickListener listener = new ValidatingOnClickListener(theDialog);
            b.setOnClickListener(listener);

            getEditText().setOnEditorActionListener(listener);
            getEditText().addTextChangedListener(listener);
        }
    }

    private final class ValidatingOnClickListener implements View.OnClickListener, TextView.OnEditorActionListener, TextWatcher
    {
        private final AlertDialog theDialog;
        private ValidatingOnClickListener(AlertDialog theDialog)
        {
            this.theDialog = theDialog;
        }

        @Override
        public void onClick(View view)
        {
            performValidation();
        }

        public void performValidation()
        {
            String text = getEditText().getText().toString();
            TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
            if(mValidationType == VALIDATION_PATH)
            {
                if(! text.matches("^(\\/[^\\/]+)+\\/{1}$"))
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.settings_path_invalid_slash));
                }
                else if(! text.matches("^(\\/[a-zA-Z\\d\\-\\_]+)+\\/{1}$"))
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.settings_path_invalid_chars));
                }
                else
                {
                    theDialog.dismiss();
                    ValidatingEditTextPreference.this.onClick(theDialog, AlertDialog.BUTTON_POSITIVE);
                }
            }
            else if(mValidationType == VALIDATION_URL)
            {
                Pattern p = Pattern.compile("^(http[s]*?:\\/\\/([\\w\\d]+\\.)?)*(www.)*dropbox\\.com\\/sh\\/");
                Matcher m = p.matcher(text);
                if(! m.find())
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.settings_url_invalid));
                }
                else
                {
                    theDialog.dismiss();
                    ValidatingEditTextPreference.this.onClick(theDialog, AlertDialog.BUTTON_POSITIVE);
                }
            }
            //if(editText.getText().toString().matches("[\\s\\S]*\\S[\\s\\S]*"))
            /*if(text.matches("^(\\/[a-zA-Z\\d\\-\\_]+)+\\/{1}$"))
            {
                Log.d(TAG, "Matches the pattern");
                theDialog.dismiss();
                ValidatingEditTextPreference.this.onClick(theDialog, AlertDialog.BUTTON_POSITIVE);
            }
            else
            {
                Log.d(TAG, "Does not match the pattern");
                TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
                textInputLayout.setEnabled(true);
                textInputLayout.setError(getContext().getString(R.string.settings_path_invalid));
            }*/
        }

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
        {
            Log.d(TAG, "Action");
            TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
            textInputLayout.setErrorEnabled(false);
            return false;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            Log.d(TAG, "Text changed");
            TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
            textInputLayout.setErrorEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable editable)
        {

        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {

        }
    }
}
