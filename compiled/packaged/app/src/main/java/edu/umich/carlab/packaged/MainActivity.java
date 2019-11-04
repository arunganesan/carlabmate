package edu.umich.carlab.packaged;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import edu.umich.carlab.CLService;
import edu.umich.carlab.Constants;

import static edu.umich.carlab.Constants.CLSERVICE_STOPPED;
import static edu.umich.carlab.Constants.DONE_INITIALIZING_CL;
import static edu.umich.carlab.Constants.ManualChoiceKey;
import static edu.umich.carlab.Constants.STATUS_CHANGED;


public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";
    CLService carlabService;
    boolean mBound = false;
    SharedPreferences prefs;
    TextView statusText;
    BroadcastReceiver clStopped = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            statusText.setText("Stopped");
        }
    };
    BroadcastReceiver clStarted = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            statusText.setText("Started");
        }
    };
    ToggleButton toggleButton;
    BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            statusText.setText("Update...");
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            CLService.LocalBinder binder = (CLService.LocalBinder) service;
            carlabService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            carlabService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wireUI();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(Constants.Session_State_Key, 1).apply();

    }

    @Override
    public void onResume () {
        super.onResume();
        bindService(new Intent(this, PackageCLService.class), mConnection,
                    Context.BIND_AUTO_CREATE);

        registerReceivers();
    }

    @Override
    public void onStop () {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }

        unregisterReceivers();
    }

    void registerReceivers () {
        registerReceiver(clStopped, new IntentFilter(CLSERVICE_STOPPED));
        registerReceiver(updateReceiver, new IntentFilter(STATUS_CHANGED));
        registerReceiver(clStarted, new IntentFilter(DONE_INITIALIZING_CL));
    }

    void unregisterReceivers () {
        unregisterReceiver(clStopped);
        unregisterReceiver(updateReceiver);
        unregisterReceiver(clStarted);
    }

    void wireUI () {
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(ManualChoiceKey, isChecked).apply();
                sendBroadcast(new Intent(MainActivity.this, PackageTriggerSession.class));
                if (isChecked) {
                    toggleButton.setEnabled(false);
                    statusText.setText("Starting");
                }
            }
        });
    }
}
