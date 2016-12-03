package com.roundel.fizyka;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.support.design.widget.TextInputLayout;
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
import android.widget.Toast;

import com.roundel.fizyka.dropbox.DropboxLinkValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by RouNdeL on 2016-09-30.
 */
public class ValidatingEditTextPreference extends EditTextPreference
{
    private String TAG = "PreferenceValidator";
    private final String POSITIVE_BUTTON_TAG = "positiveButton";
    private final String NEUTRAL_BUTTON_TAG = "neutralButton";
    private EditText mEditText;
    private int mValidationType;
    private final int VALIDATION_PATH = 0;
    private final int VALIDATION_URL = 1;

    public ValidatingEditTextPreference(Context context)
    {
        super(context);
        //Auto-generated constructor stub
    }

    public ValidatingEditTextPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ValidatingEditTextPreference, 0, 0);

        try
        {
            mValidationType = a.getInteger(R.styleable.ValidatingEditTextPreference_type, VALIDATION_PATH);
        }
        finally
        {
            a.recycle();
        }
    }

    public ValidatingEditTextPreference(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        //Auto-generated constructor stub
    }


    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText)
    {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.text_input_layout);
        if(container != null)
        {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder)
    {
        super.onPrepareDialogBuilder(builder);
        if(mValidationType == VALIDATION_URL)
            builder.setNeutralButton(getContext().getString(R.string.validation_url_button), new ValidatingOnClickListener(((AlertDialog) getDialog())));
    }

    @Override
    protected void showDialog(Bundle state)
    {
        super.showDialog(state);
        if(super.getDialog() instanceof AlertDialog)
        {
            final AlertDialog dialog = (AlertDialog) super.getDialog();
            EditText editText = getEditText();

            Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button buttonNeutral = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            ValidatingOnClickListener listener = new ValidatingOnClickListener(dialog);
            buttonPositive.setOnClickListener(listener);
            buttonNeutral.setOnClickListener(listener);
            buttonPositive.setTag(POSITIVE_BUTTON_TAG);
            buttonNeutral.setTag(NEUTRAL_BUTTON_TAG);

            editText.setOnEditorActionListener(listener);
            editText.addTextChangedListener(listener);
            editText.setSingleLine(true);
            if(mValidationType == VALIDATION_URL) editText.selectAll();
        }
    }

    private final class ValidatingOnClickListener implements View.OnClickListener, TextView.OnEditorActionListener, TextWatcher, DialogInterface.OnClickListener
    {
        private final AlertDialog theDialog;

        private ValidatingOnClickListener(AlertDialog theDialog)
        {
            this.theDialog = theDialog;
        }

        @Override
        public void onClick(View view)
        {
            if(view.getTag().equals(POSITIVE_BUTTON_TAG))
            {
                regexValidation();
            }
            else if(view.getTag().equals(NEUTRAL_BUTTON_TAG))
            {
                urlValidation();
            }
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
        }

        public void regexValidation()
        {
            String text = getEditText().getText().toString();
            TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
            if(mValidationType == VALIDATION_PATH)
            {
                if(!text.matches("^(\\/[^\\/]+)+\\/{1}$"))
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.validation_path_invalid_slash));
                }
                else if(!text.matches("^(\\/[a-zA-Z\\d\\-\\_]+)+\\/{1}$"))
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.validation_path_invalid_chars));
                }
                else
                {
                    theDialog.dismiss();
                    ValidatingEditTextPreference.this.onClick(theDialog, AlertDialog.BUTTON_POSITIVE);
                }
            }
            else if(mValidationType == VALIDATION_URL)
            {
                Pattern p = Pattern.compile("^(http[s]*?:\\/\\/)?(www.)?dropbox\\.com\\/sh\\/");
                Matcher m = p.matcher(text);
                if(text.isEmpty())
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.validation_url_empty));
                }
                else if(!m.find())
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.validation_url_invalid_regex));
                }
                else
                {
                    theDialog.dismiss();
                    ValidatingEditTextPreference.this.onClick(theDialog, AlertDialog.BUTTON_POSITIVE);
                }
            }
        }

        public void urlValidation()
        {
            if(mValidationType == VALIDATION_URL)
            {
                final TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
                final String text = getEditText().getText().toString();
                Pattern p = Pattern.compile("^(http[s]*?:\\/\\/([\\w\\d]+\\.)?)*(www.)*dropbox\\.com\\/sh\\/");
                Matcher m = p.matcher(text);
                if(text.isEmpty())
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.validation_url_empty));
                }
                else if(!m.find())
                {
                    textInputLayout.setErrorEnabled(true);
                    textInputLayout.setError(getContext().getString(R.string.validation_url_invalid_regex));
                }
                else
                {
                    Connectivity.hasAccess(new Connectivity.onHasAccessResponse()
                    {
                        @Override
                        public void onConnectionCheckStart()
                        {

                        }

                        @Override
                        public void onConnectionAvailable(Long responseTime)
                        {
                            DropboxLinkValidator validator = new DropboxLinkValidator(getContext(), new DropboxLinkValidator.DropboxLinkValidatorListener()
                            {
                                @Override
                                public void onTaskStart()
                                {
                                    Toast.makeText(getContext(), getContext().getString(R.string.validation_url_refresh), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onTaskEnd(String result)
                                {
                                    Log.d(TAG, result);
                                    switch(result)
                                    {
                                        case DropboxLinkValidator.NO_ERROR:
                                            textInputLayout.setErrorEnabled(false);
                                            Toast.makeText(getContext(), getContext().getString(R.string.validation_url_ok), Toast.LENGTH_SHORT).show();
                                            break;
                                        case DropboxLinkValidator.ERROR_NOT_FOUND:
                                            textInputLayout.setErrorEnabled(true);
                                            textInputLayout.setError(getContext().getString(R.string.validation_url_not_found));
                                            break;
                                        case DropboxLinkValidator.ERROR_FORBIDDEN:
                                            textInputLayout.setErrorEnabled(true);
                                            textInputLayout.setError(getContext().getString(R.string.validation_url_no_access));
                                            break;
                                        case DropboxLinkValidator.ERROR_UNKNOWN:
                                            textInputLayout.setErrorEnabled(true);
                                            textInputLayout.setError(getContext().getString(R.string.validation_url_unknown));
                                            break;
                                        case DropboxLinkValidator.ERROR_CONNECTION_TIMED_OUT:
                                            Toast.makeText(getContext(), getContext().getString(R.string.toast_error_connection_timed_out), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            validator.execute(getContext().getString(R.string.api_url), text);
                        }

                        @Override
                        public void onConnectionUnavailable()
                        {
                            Toast.makeText(getContext(), getContext().getString(R.string.toast_no_network), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
        {
            TextInputLayout textInputLayout = (TextInputLayout) theDialog.findViewById(R.id.text_input_layout);
            textInputLayout.setErrorEnabled(false);
            return false;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
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
