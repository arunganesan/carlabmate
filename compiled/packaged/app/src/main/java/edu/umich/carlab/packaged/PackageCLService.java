package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.Constants;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;
import edu.umich.carlab.loadable.AlgorithmSpecs.AlgorithmInformation;
import edu.umich.watchfone.WatchFone;

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
    public void loadRequirements () {
        Algorithm alignedIMU = new AlignedIMU(null, null);
        strategyRequirements.add(new AlgorithmInformation(alignedIMU,
                                                          new AlgorithmSpecs.InfoWorldAlignedGyro(
                                                                  true)));
        strategyRequirements.add(new AlgorithmInformation(alignedIMU,
                                                          new AlgorithmSpecs.InfoWorldAlignedAccel(
                                                                  true)));
        strategyRequirements
                .add(new AlgorithmInformation(alignedIMU, new AlgorithmSpecs.InfoRotation(false)));


        Algorithm watchfone = new WatchFone(null, null);
        strategyRequirements
                .add(new AlgorithmInformation(watchfone, new AlgorithmSpecs.InfoCarGear(false)));

        strategyRequirements
                .add(new AlgorithmInformation(watchfone, new AlgorithmSpecs.InfoCarRPM(false)));

        strategyRequirements
                .add(new AlgorithmInformation(watchfone, new AlgorithmSpecs.InfoCarFuel(false)));

        strategyRequirements.add(new AlgorithmInformation(watchfone,
                                                          new AlgorithmSpecs.InfoCarOdometer(
                                                                  false)));
        strategyRequirements
                .add(new AlgorithmInformation(watchfone, new AlgorithmSpecs.InfoCarSpeed(false)));

        strategyRequirements.add(new AlgorithmInformation(watchfone,
                                                          new AlgorithmSpecs.InfoCarSteering(
                                                                  false)));
    }

}
