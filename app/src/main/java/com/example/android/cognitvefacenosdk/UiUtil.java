package com.example.android.cognitvefacenosdk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UiUtil {
    public static void showMessageOKCancel(Context context, int stringId, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(stringId)
                .setPositiveButton(R.string.ok, okListener)
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }
}
