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
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.carlab.CLService;
import edu.umich.carlab.Constants;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.io.AppLoader;
import edu.umich.carlab.loadable.AlgorithmSpecs;
import edu.umich.carlab.loadable.App;
import edu.umich.carlabui.AppsAdapter;

import static edu.umich.carlab.Constants.CARLAB_STATUS;
import static edu.umich.carlab.Constants.GATEWAY_STATUS;
import static edu.umich.carlab.Constants.ManualChoiceKey;
import static edu.umich.carlab.Constants._STATUS_MESSAGE;
import static edu.umich.carlabui.AppsAdapter.AppState.ACTIVE;


public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";
    CLService carlabService;
    TextView carlabStatusText, gatewayStatusText;
    GridView algorithmsGridView;
    List<AppsAdapter.AppModel> appModels;
    AppsAdapter appsAdapter;
    Map<String, Integer> appModelIndexMap;
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


    boolean initializedGrid = false;
    boolean mBound = false;
    SharedPreferences prefs;
    ToggleButton toggleButton;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            CLService.LocalBinder binder = (CLService.LocalBinder) service;
            carlabService = binder.getService();
            mBound = true;

            if (!initializedGrid)
                initializeGrid();
        }

        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            carlabService = null;
            mBound = false;
        }
    };

    public void initializeGrid(){

        appModels = new ArrayList<>();
        appModelIndexMap = new HashMap<>();

        // If CarLab is running, we can create live middleware links
        // Otherwise, we will just use the static app loader
        // List<App> allApps = AppLoader.getInstance().instantiateApps(null, null);


        Set<AlgorithmSpecs.AlgorithmInformation> strategy = carlabService.getStrategy();
        Set<String> uniqueAppNames = new HashSet<>();
        Set<App> uniqueApps = new HashSet<>();
        for (AlgorithmSpecs.AlgorithmInformation alginfo : strategy) {
            if (!uniqueAppNames.contains(alginfo.algorithm.getName())) {
                uniqueAppNames.add(alginfo.algorithm.getName());
                uniqueApps.add(alginfo.algorithm);
            }
        }

        for (App app : uniqueApps) {
            AppsAdapter.AppState appState = ACTIVE;
            appModels.add(new AppsAdapter.AppModel(
                    app.getName(),
                    app.getClass().getCanonicalName(),
                    appState));
        }

        // This index is useful later
        for (int i = 0; i < appModels.size(); i++) {
            String classname = appModels.get(i).className;
            appModelIndexMap.put(classname, i);
        }

        appsAdapter = new AppsAdapter(this, appModels);
        algorithmsGridView.setAdapter(appsAdapter);
        initializedGrid = true;
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wireUI();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt(Constants.Session_State_Key, 1).apply();
        // Delete all files from locally for now.
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
        registerReceiver(clStatusChanged, new IntentFilter(CARLAB_STATUS));
        registerReceiver(gatewayStatusChanged, new IntentFilter(GATEWAY_STATUS));
        registerReceiver(appStateReceiver, new IntentFilter(edu.umich.carlab.Constants.INTENT_APP_STATE_UPDATE));

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

    }


    private BroadcastReceiver appStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //activeApps = prefs.getStringSet(Constants.ACTIVE_APPS_KEY, new HashSet<String>());

            String appClassName = intent.getStringExtra("appClassName");
            DataMarshal.MessageType appState = (DataMarshal.MessageType) intent.getSerializableExtra("appState");

            // This means we haven't set up this app yet
            if (appModelIndexMap == null) return;
            if (!appModelIndexMap.containsKey(appClassName))
                return;

            int appIndex = appModelIndexMap.get(appClassName);

            switch (appState) {
                case ERROR:
                    appModels.get(appIndex).state = AppsAdapter.AppState.ERROR;
                    break;
                case DATA:
                    appModels.get(appIndex).state = AppsAdapter.AppState.DATA;
                    break;
                case STATUS:
                    appModels.get(appIndex).state = AppsAdapter.AppState.PROCESSING;
            }

            appsAdapter.notifyDataSetChanged();
        }
    };
}


