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

    // Need to change the Arrays.asList() for loaded algorithms
    /*
    Should switch to:

    loadedAlgorithms = new ArrayList<>();
    loadedAlgorithms.add(carlab.android_passthroughs.Algorithm.class);
    */
    public class PackageStrategy extends Strategy {
        public PackageStrategy () {
            loadedAlgorithms = Arrays.asList(carlab.android_passthroughs.Algorithm.class, carlab.obd_devices.Algorithm.class);
            loadedFunctions = Arrays.asList(carlab.android_passthroughs.Algorithm.getLocation, carlab.obd_devices.Algorithm.readFuelLevel);
            saveInformation = Arrays.asList(Registry.Location, Registry.CarFuel);
        }
    }
}
