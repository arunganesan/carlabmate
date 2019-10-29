package edu.umich.sandbox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import edu.umich.carlab.clog.CLogDatabaseHelper;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlabui.ExperimentBaseActivity;

import static edu.umich.carlab.Constants.*;


public class Sandbox extends ExperimentBaseActivity {
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs
                .edit()
//                .putString(UID_key, getString(edu.umich.carlab.watchfon.R.string.uid))
                .putBoolean(LIVE_MODE, true)
//                .putInt(Experiment_Id, getApplication().getResources().getInteger(edu.umich.carlab.watchfon.R.integer.experimentID))
//                .putInt(Experiment_Version_Number, getApplication().getResources().getInteger(edu.umich.carlab.watchfon.R.integer.version))
//                .putString(Experiment_Shortname, getString(edu.umich.carlab.watchfon.R.string.shortname))
                .putBoolean(Experiment_New_Version_Detected, false)
                .putString(Main_Activity, Sandbox.class.getCanonicalName())
                .commit();

        super.onCreate(savedInstanceState);

        CLogDatabaseHelper.initializeIfNeeded(this);
        AppLoader instance = AppLoader.getInstance();

        instance.loadApps(new Class<?>[]{
                edu.umich.aligned_imu.AlignedIMU.class,
        });
    }
}