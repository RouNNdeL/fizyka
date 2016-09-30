package com.roundel.fizyka;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.DialogFragment;

public class RestartDialogFragment extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        String title = args.getString("TITLE");
        String message = args.getString("MESSAGE");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface p1, int p2)
            {
                SharedPreferences preferences = getActivity().getSharedPreferences("com.roundel.fizyka_preferences", getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                String date = preferences.getString("date", "Thu, 1 Jan 1970 00:00:00 +0000");
                editor.clear();
                editor.putString("date", date);
                editor.commit();
                Intent i = getActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage( getActivity().getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface p1, int p2)
            {

            }
        });

        return builder.create();
    }

}
