package edu.umich.carlab.packaged;

import android.content.ComponentName;
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
    Map<Algorithm, Set<String>> algorithmInputWiring = new HashMap<>();
    CLService carlabService;
    boolean mBound = false;
    Set<String> saveInformation = new HashSet<>();
    Set<AlgorithmInformation> strategyRequirements = new HashSet<>();
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected (ComponentName className, IBinder service) {
            CLService.LocalBinder binder = (CLService.LocalBinder) service;
            carlabService = binder.getService();
            for (Map.Entry<Algorithm, Set<String>> wiring : algorithmInputWiring.entrySet())
                for (String info : wiring.getValue())
                    carlabService.addMultiplexRoute(info, wiring.getKey());
            mBound = true;
        }

        @Override
        public void onServiceDisconnected (ComponentName arg0) {
            carlabService = null;
        }
    };

    void initializeRouting () {
        Algorithm alignedIMU = new AlignedIMU(null, this);
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "world-aligned-gyro"));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "world-aligned-accel"));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU, "rotation"));

        saveInformation.add("world-aligned-accel");
        saveInformation.add("world-aligned-gyro");

        // For all requirements
        for (AlgorithmInformation algorithmInformation : strategyRequirements) {

            // Get the function which produces that
            for (AlgorithmSpecs.AppFunction function : alignedIMU.algorithmFunctions)

                // Make sure that this algorithm gets all the INPUT to that function
                // Just make sure it's wired. Nothing fancy.
                if (function.outputInformation.equals(algorithmInformation.information)) {
                    if (!algorithmInputWiring.containsKey(algorithmInformation.algorithm))
                        algorithmInputWiring
                                .put(algorithmInformation.algorithm, new HashSet<String>());

                    algorithmInputWiring.get(algorithmInformation.algorithm)
                                        .addAll(function.inputInformation);

                    break;
                }
        }
    }

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Algorithm algorithm = new AlignedIMU(null, this);
        List<AlgorithmSpecs.AppFunction> functions = algorithm.algorithmFunctions;

        // 1. Routing
        initializeRouting();

        // 2. Start/stop carlab (manual or automatic)
        // 3. Send and receive from linkserver
    }

    public class AlgorithmInformation {
        public Algorithm algorithm;
        public String information;

        public AlgorithmInformation (Algorithm a, String i) {
            algorithm = a;
            information = i;
        }
    }
}
