package com.example.test.chinesechess;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

public class Helpers {
    // list of common function that can be used by all applications
    Context context;
    Activity activity;

    // shared preference:
    SharedPreferences prop;
    SharedPreferences.Editor prefEditor;

    // for alert dialog
    AlertDialog dialog;

    // constructor
    public Helpers(final Context context, final Activity activity) {
        this.context = context;
        this.activity = activity;

        // shared pref:
        prop = activity.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        prefEditor = prop.edit();
    }


    //////////////////////////////////////////////
    // member methods.
    public SharedPreferences getSharedPrefere() {
        return prop;
    }

    public SharedPreferences.Editor getSharedPreferencesEditor() {
        return prefEditor;
    }

    // parse db and get the needed information
    public Object getFromDB(DataSnapshot dataSnapshot, String key) {
        // get object from snapshot.
        Iterable<DataSnapshot> data = dataSnapshot.getChildren();
        Object obj = null;

        for(DataSnapshot snapshot:data){
            if( snapshot.getKey().equals(key) ){
                obj = snapshot.getValue();
            }
        }

        return obj;
    }

    /////////////////////////////////////////////////
    // alert Dialogs
    // called at onBackPressed()
    public void backPressAction(){
        // display confirmation dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Exit");
        builder.setMessage("Exit game?");

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        builder.setPositiveButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if true then exit.
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(a);
                activity.finish();
            }
        });

        // todo : update shared pref?

        dialog = builder.create();
        dialog.show();
    }

    // denied display dialog
    public void denyAlertDialog(String action){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(action + " denied!");
        builder.setMessage("Opponent does not allow you to " + action + "!");

        dialog = builder.create();
        dialog.show();
    }

    // confirm alert dialog
    // returns true if confirmed.
    public void confirmAlertDialog(final String action, final DatabaseReference uiAnsRef1, final DatabaseReference uiAnsRef2){
        // display confirmation dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(action.toUpperCase());
        builder.setMessage("Allow opponent to " + action + "?");

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                uiAnsRef1.setValue("deny"+action);
                uiAnsRef2.setValue("deny"+action);
            }
        });

        builder.setPositiveButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uiAnsRef1.setValue("deny"+action);
                        uiAnsRef2.setValue("deny"+action);
                    }
                });

        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // if true then exit.
                uiAnsRef1.setValue(action);
                uiAnsRef2.setValue(action);
            }
        });

        // todo : update shared pref?

        dialog = builder.create();
        dialog.show();
    }

    /////////////////////////////////////////////////7
    // close phone keyboard
    public void closeKeyboard() {
        View view = activity.getCurrentFocus();
        if(view!=null){
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    public void openKeyboard(EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    // vibrate device
    // in manifest:
    // <uses-permission android:name="android.permission.VIBRATE" />
    public void vibrateDevice() {
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);

        v.vibrate(500);
    }

    // get Drawable from name string:
    public Drawable getDrawableFromName(String name){
        int charPicId;
        Drawable drawable;

        charPicId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        drawable = context.getResources().getDrawable(charPicId);
        return drawable;
    }
}
