package edu.umich.carlab.template;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import edu.umich.carlab.clog.CLog;
import edu.umich.carlab.clog.CLogDatabaseHelper;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.utils.Utilities;
import edu.umich.carlab.world_aligned_imu.AppImpl;
import edu.umich.carlab.world_aligned_imu.MiddlewareImpl;
import edu.umich.carlabui.CarLabUIBuilder;
import edu.umich.carlabui.R;


import static edu.umich.carlab.Constants.*;
import static edu.umich.carlab.template.Constants.ManualChoiceKey;

public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";

    SharedPreferences prefs;
    Class<?> mainDisplayClass;
    int version, experimentID;
    String shortname = "";

    Class<?> triggerClass;
    CarLabUIBuilder uiBuilder;
    Button triggerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customizable);
        View contentView = findViewById(android.R.id.content);

        String devAddr = "";

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String personID;
        CLogDatabaseHelper.initializeIfNeeded(this);


        personID = getString(edu.umich.carlab.template.R.string.uid);


        /*****************************************************************
         * Begin per-user customization
         *****************************************************************/
        mainDisplayClass = PhoneCollectApp.class;
        triggerClass = ManualTrigger.class;



        AppLoader instance = AppLoader.getInstance();
        instance.loadApp(PhoneCollectApp.class);
        instance.loadApp(AppImpl.class);
        instance.loadMiddleware(new MiddlewareImpl());

        experimentID = getApplication().getResources().getInteger(edu.umich.carlab.template.R.integer.experimentID);
        version = getApplication().getResources().getInteger(edu.umich.carlab.template.R.integer.version);
        shortname = getString(edu.umich.carlab.template.R.string.shortname);

        CLog.v(TAG, "Main display class = " + mainDisplayClass.getName());
        CLog.v(TAG, "Trigger class = " + triggerClass.getName());
        CLog.v(TAG, String.format("Start. UID=%s, SDK=%d", personID, Build.VERSION.SDK_INT));

        prefs
                .edit()
                .putInt(Experiment_Id, experimentID)
                .putInt(Experiment_Version_Number, version)
                .putString(Experiment_Shortname, shortname)
                .putBoolean(Experiment_New_Version_Detected, false)
                .putString(Main_Activity, MainActivity.class.getCanonicalName())
                .apply();

        uiBuilder = new CarLabUIBuilder(this, contentView, personID, devAddr, version, mainDisplayClass);
        /**************************************************************/

        Utilities.scheduleOnce(this, triggerClass, 0);
        uiBuilder.onCreate();
        addManualTriggerButton();

    }


    void addManualTriggerButton() {
        FrameLayout layout = findViewById(R.id.moduleSpecific);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        triggerButton = new Button(this);
        triggerButton.setLayoutParams(params);
        layout.addView(triggerButton);

        triggerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isOn = prefs.getBoolean(ManualChoiceKey, false);
                prefs.edit().putBoolean(ManualChoiceKey, !isOn).commit();
                updateTriggerButton();
                sendBroadcast(new Intent(MainActivity.this, triggerClass));
            }
        });
    }


    void updateTriggerButton() {
        boolean isOn = prefs.getBoolean(ManualChoiceKey, false);
        triggerButton.setText(isOn ? "Turn Off" : "Turn On");
    }


    @Override
    public void onResume() {
        super.onResume();
        uiBuilder.onResume();
        updateTriggerButton();

    }




    @Override
    public void onPause() {
        super.onPause();
        uiBuilder.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
        uiBuilder.onStop();
    }

}
