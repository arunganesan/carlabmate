package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.Constants;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;
import edu.umich.carlab.loadable.AlgorithmSpecs.AlgorithmInformation;

public class PackageCLService extends edu.umich.carlab.CLService {
    public static void turnOffCarLab (Context context) {
        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_OFF);
        context.startService(intent);
    }

    public static void turnOnCarLab (Context context) {
        // This means we havent' connected in a while.
        // And this re-establishment isn't due to a temporary break
        // And we just connected to the actual OBD device

        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    protected void initializeRouting () {
         /*
        [
            // Says we want to use THIS algorithm to get THIS information
            { "algorithm": "aligned-imu", "information": "world-aligned-gyro", "save": true },
            { "algorithm": "aligned-imu", "information": "world-aligned-accel", "save": true },
            { "algorithm": "aligned-imu", "information": "rotation" },
        ]
         */

        Algorithm alignedIMU = new AlignedIMU(this, this);
        strategyRequirements.add(new AlgorithmInformation(alignedIMU,
                                                          new AlgorithmSpecs.InfoWorldAlignedGyro(
                                                                  true)));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU,
                                                          new AlgorithmSpecs.InfoWorldAlignedAccel(
                                                                  true)));
        strategyRequirements
                .add(new AlgorithmInformation(alignedIMU, new AlgorithmSpecs.InfoRotation(false)));
        saveInformation.add("world-aligned-accel");
        saveInformation.add("world-aligned-gyro");
        super.initializeRouting();
    }


}
