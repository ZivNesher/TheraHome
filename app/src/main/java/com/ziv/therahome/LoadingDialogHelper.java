package com.ziv.therahome;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.ziv.therahome.R;

public class LoadingDialogHelper {
    private AlertDialog dialog;

    public void show(Activity activity) {
        if (dialog != null && dialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
    }

    public void hide() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}

