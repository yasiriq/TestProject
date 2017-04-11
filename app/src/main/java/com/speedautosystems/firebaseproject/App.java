package com.speedautosystems.firebaseproject;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Yasir on 2/13/2017.
 */
public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseApp.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
