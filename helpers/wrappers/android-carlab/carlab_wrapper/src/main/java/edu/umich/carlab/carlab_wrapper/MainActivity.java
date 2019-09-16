package edu.umich.carlab.carlab_wrapper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.loadable.Middleware;
import edu.umich.carlabui.ExperimentBaseActivity;

import static edu.umich.carlab.Constants.*;

public class MainActivity extends ExperimentBaseActivity {
    final String TAG = "MainActivity";

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putBoolean(LIVE_MODE, true)
                .putString(Experiment_Shortname, getString(R.string.app_name))
                .putString(Main_Activity, MainActivity.class.getCanonicalName())
                .commit();

        super.onCreate(savedInstanceState);

        AppLoader instance = AppLoader.getInstance();
        instance.loadApps(new Class<?>[]{
                /*
                // For each implementation used here, load them
                // The AppImpl class actually receives data
                e.g. edu.umich.carlab.world_aligned_imu.AppImpl.class,
                */
        });


        instance.loadMiddlewares(new Middleware[]{
                /*
                // For each implementation, load their middleware as well
                // Middleware has info such as how to split the data or what data is output by this guy
                // That is used when saving, or when other people want to use the data
                e.g. new edu.umich.carlab.world_aligned_imu.MiddlewareImpl(),
                */
        });
    }
}
