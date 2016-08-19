package com.umanets.seconfdemoapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ko3ak_zhn on 8/15/16.
 */
public class FirebaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
       FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
