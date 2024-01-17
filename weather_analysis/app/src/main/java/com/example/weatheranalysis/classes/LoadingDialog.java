package com.example.weatheranalysis.classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

import com.example.weatheranalysis.R;
//τάξη για την εμφάνιση loading
public class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void loadDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }
    public void removeDialog(){
        dialog.dismiss();
    }
}
