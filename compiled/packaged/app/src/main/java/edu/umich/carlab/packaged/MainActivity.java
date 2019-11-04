package edu.umich.carlab.packaged;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.ToggleButton;

import edu.umich.carlab.CLService;


public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";
    CLService carlabService;
    boolean mBound = false;
    TextView statusText;
    ToggleButton toggleButton;

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
        }
    };


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wireUI();
    }

    @Override
    public void onResume () {
        super.onResume();
        bindService(new Intent(this, PackageCLService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop () {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    void wireUI () {
        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);
    }
}
