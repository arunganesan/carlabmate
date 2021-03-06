package edu.umich.carlab.packaged;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umich.carlab.CLService;
import edu.umich.carlab.Constants;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;


import static edu.umich.carlab.Constants.CARLAB_STATUS;
import static edu.umich.carlab.Constants.GATEWAY_STATUS;
import static edu.umich.carlab.Constants.ManualChoiceKey;
import static edu.umich.carlab.Constants.SESSION;
import static edu.umich.carlab.Constants._STATUS_MESSAGE;
import static edu.umich.carlab.packaged.AppsAdapter.AppState.ACTIVE;


public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";
    GridView algorithmsGridView;
    Map<String, Integer> appModelIndexMap;
    List<AppsAdapter.AppModel> appModels;
    AppsAdapter appsAdapter;
    CLService carlabService;
    TextView carlabStatusText, gatewayStatusText;
    BroadcastReceiver clStatusChanged = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            carlabStatusText.setText(intent.getStringExtra(_STATUS_MESSAGE));
        }
    };
    BroadcastReceiver gatewayStatusChanged = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            gatewayStatusText.setText(intent.getStringExtra(_STATUS_MESSAGE));
        }
    };
    Map<String, List<Integer>> infoFunctionIndexMapping;
    boolean initializedGrid = false;
    boolean mBound = false;
    SharedPreferences prefs;
    ToggleButton toggleButton;
    private BroadcastReceiver appStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive (Context context, Intent intent) {
            String infoname = intent.getStringExtra("information");

            if (infoFunctionIndexMapping == null) return;
            if (infoFunctionIndexMapping.containsKey(infoname)) {
                for (int appIndex : infoFunctionIndexMapping.get(infoname)) {
                    appModels.get(appIndex).state = AppsAdapter.AppState.DATA;
                }
            }

            appsAdapter.notifyDataSetChanged();
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            CLService.LocalBinder binder = (CLService.LocalBinder) service;
            carlabService = binder.getService();
            mBound = true;

            if (!initializedGrid) initializeGrid();
        }

        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            carlabService = null;
            mBound = false;
        }
    };

    public void initializeGrid () {
        appModels = new ArrayList<>();
        appModelIndexMap = new HashMap<>();
        infoFunctionIndexMapping = new HashMap<>();

        List<Algorithm.Function> functions = carlabService.getLoadedFunctions();

        for (Algorithm.Function func : functions) {
            AppsAdapter.AppState appState = ACTIVE;
            appModels.add(new AppsAdapter.AppModel(func.name, func.inputInformation, appState));
        }

        // This index is useful later
        for (int i = 0; i < appModels.size(); i++)
            for (Registry.Information info : appModels.get(i).inputInformation) {
                if (!infoFunctionIndexMapping.containsKey(info.name))
                    infoFunctionIndexMapping.put(info.name, new ArrayList<Integer>());
                infoFunctionIndexMapping.get(info.name).add(i);
            }

        appsAdapter = new AppsAdapter(this, appModels);
        algorithmsGridView.setAdapter(appsAdapter);
        initializedGrid = true;
    }
    /**
     * Function to get permission for location
     * Ideally this happens in PhoneSensors where the GPS sensor is turned on
     * But, since we need an Activity handle, this might be the most reasonable place to do it for now
     * https://developer.android.com/training/permissions/requesting.html#java
     */
    void checkAndRequestLocPermission() {
        // Here, thisActivity is the current activity
        if ((ContextCompat.checkSelfPermission(MainActivity.this,
                                               Manifest.permission.ACCESS_FINE_LOCATION)
             != PackageManager.PERMISSION_GRANTED)

            ||

            (ContextCompat.checkSelfPermission(MainActivity.this,
                                               Manifest.permission.ACCESS_COARSE_LOCATION)
             != PackageManager.PERMISSION_GRANTED)) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                                                     Manifest.permission.ACCESS_FINE_LOCATION)) {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                                                  new String[]{
                                                          Manifest.permission.ACCESS_FINE_LOCATION,
                                                          Manifest.permission.ACCESS_COARSE_LOCATION
                                                  },
                                                  edu.umich.carlab.Constants.TAG_CODE_PERMISSION_LOCATION);
            }
        }
    }
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wireUI();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(Constants.Session_State_Key, 1).apply();

        checkAndRequestLocPermission();
    }

    @Override
    public void onResume () {
        super.onResume();
        bindService(new Intent(this, PackageCLService.class), mConnection,
                    Context.BIND_AUTO_CREATE);

        checkLogin();
        registerReceivers();
    }

    void checkLogin() {
        if (prefs.getString(SESSION, null) == null) {
            Intent loginIntent = new Intent(this, Login.class);
            startActivityForResult(loginIntent, 2);
        }
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
        registerReceiver(clStatusChanged, new IntentFilter(CARLAB_STATUS));
        registerReceiver(gatewayStatusChanged, new IntentFilter(GATEWAY_STATUS));
        registerReceiver(appStateReceiver,
                         new IntentFilter(edu.umich.carlab.Constants.INTENT_APP_STATE_UPDATE));

    }

    void unregisterReceivers () {
        unregisterReceiver(clStatusChanged);
        unregisterReceiver(gatewayStatusChanged);
        unregisterReceiver(appStateReceiver);
    }

    void wireUI () {
        carlabStatusText = findViewById(R.id.carlabStatusText);
        gatewayStatusText = findViewById(R.id.gatewayStatusText);
        algorithmsGridView = findViewById(R.id.algorithm_grid);
        toggleButton = findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(ManualChoiceKey, isChecked).apply();
                sendBroadcast(new Intent(MainActivity.this, PackageTriggerSession.class));
                Intent i = new Intent(CARLAB_STATUS);
                i.putExtra(_STATUS_MESSAGE, "Starting");
                sendBroadcast(i);
            }
        });


        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                prefs.edit().putString(Constants.SESSION, null).apply();
                Intent loginIntent = new Intent(MainActivity.this, Login.class);
                startActivityForResult(loginIntent, 2);
            }
        });

    }
}


