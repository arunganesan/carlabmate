package edu.umich.sandbox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import edu.umich.carlab.io.AppLoader;
import edu.umich.carlabui.SandboxActivity;

import static edu.umich.carlab.Constants.*;


public class Sandbox extends SandboxActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs
                .edit()
//                .putString(UID_key, getString(edu.umich.carlab.watchfon.R.string.uid))
                .putBoolean(LIVE_MODE, true)
                .putBoolean(Experiment_New_Version_Detected, false)
                .putString(Main_Activity, Sandbox.class.getCanonicalName())
                .commit();

        super.onCreate(savedInstanceState);

        AppLoader instance = AppLoader.getInstance();

        instance.loadApps(new Class<?>[]{
                edu.umich.aligned_imu.AlignedIMU.class,
        });
    }
}