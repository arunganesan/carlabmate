package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;

import edu.umich.carlab.Constants;
import edu.umich.carlab.Registry;
import edu.umich.carlab.Strategy;


public class PackageCLService extends edu.umich.carlab.CLService {

    public static void turnOffCarLab (Context context) {
        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_OFF);
        context.startService(intent);
    }

    public static void turnOnCarLab (Context context) {
        Intent intent = new Intent(context, PackageCLService.class);
        intent.setAction(Constants.MASTER_SWITCH_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    protected void loadRequirements () {
        strategy = new PackageStrategy();
    }

    public class PackageStrategy extends Strategy {
        public PackageStrategy () {
            loadedAlgorithms = Arrays.asList(carlab.vehicle_estimate.Algorithm.class, carlab.aligned_imu.Algorithm.class);
            loadedFunctions = Arrays.asList(carlab.vehicle_estimate.Algorithm.estimateSpeed, carlab.vehicle_estimate.Algorithm.estimateSteering, carlab.vehicle_estimate.Algorithm.estimateGear, carlab.aligned_imu.Algorithm.produceVehicleAlignedAccel, carlab.aligned_imu.Algorithm.produceGravityAlignedGyro, carlab.aligned_imu.Algorithm.produceVehiclePointingRotation);
            saveInformation = Arrays.asList(Registry.CarSpeed, Registry.CarSteering, Registry.CarGear, Registry.VehicleAlignedAccel, Registry.GravityAlignedGyro, Registry.VehiclePointingRotation);
        }
    }
}
