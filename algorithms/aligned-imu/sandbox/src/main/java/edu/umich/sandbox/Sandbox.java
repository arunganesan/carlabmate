package edu.umich.sandbox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.aligned_imu.AlignedIMUBase;
import edu.umich.carlabui.LocalAlgorithmsActivity;

import static edu.umich.carlab.Constants.Experiment_New_Version_Detected;
import static edu.umich.carlab.Constants.LIVE_MODE;
import static edu.umich.carlab.Constants.Main_Activity;

public class Sandbox extends LocalAlgorithmsActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs
                .edit()
                // .putString(UID_key, getString(edu.umich.carlab.watchfon.R.string.uid))
                .putBoolean(LIVE_MODE, true)
                .putBoolean(Experiment_New_Version_Detected, false)
                .putString(Main_Activity, Sandbox.class.getCanonicalName())
                .commit();

        super.onCreate(savedInstanceState);

        createModuleButtons(new edu.umich.aligned_imu.AlignedIMU(dataReceiver, this));
    }
}