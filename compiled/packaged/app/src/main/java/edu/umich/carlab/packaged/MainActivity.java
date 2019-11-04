package edu.umich.carlab.packaged;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.CLService;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;

/*
[
    // Says we want to use THIS algorithm to get THIS information
    { "algorithm": "aligned-imu", "information": "world-aligned-gyro", "save": true },
    { "algorithm": "aligned-imu", "information": "world-aligned-accel", "save": true },
    { "algorithm": "aligned-imu", "information": "rotation" },
]
 */


public class MainActivity extends AppCompatActivity {
    public final static String TAG = "MainActivity";
    CLService carlabService;
    boolean mBound = false;

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

        Algorithm algorithm = new AlignedIMU(null, this);
        List<AlgorithmSpecs.AppFunction> functions = algorithm.algorithmFunctions;

        // 1. Routing

        // 2. Start/stop carlab (manual or automatic)
        // How does CarLab get the algorithmInputWiring?
        // On that note, how does it get the list of Algorithms? Do we initialize them here?
        // We could... wait ... hm. We could bind to it and just call the "start" function with the actual objects
        // Then it could save the objects. Done.
        // Besides I think it does run on the same thread...
        // Either way it should all share the same virtual space.

        // 3. Send and receive from linkserver
        // This is a separate service/thread. It wakes up occasionally to get new downloads.
        // It also uploads any data we have.


        // Previously we did the manual trigger
        // Then a trigger service wakes up occasionally to check and then launch if needed
        // That still makes hella sense. Lets keep that. Use a button.
        // However! That does mean it's not trivial how we get data to CLService.
        //     Especially since we're not starting that frmo the Main Activity where it is wired together
        // IDEA: Why even use a main activity? That should just be for display.
        //     What if we extended CLService and HARD CODED the fucking things. (statically coded)
        //     That way, the ONLY purpose of main activity is to tell you some feedback or hacve a manual button
        //     Everything else starts elsewhere.
        //     No need for weird passing shit arouund.
        // Love it.
    }

    @Override
    public void onResume () {
        super.onResume();
        bindService(new Intent(this, CLService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop () {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
}
