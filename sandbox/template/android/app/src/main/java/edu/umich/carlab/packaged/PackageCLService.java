package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;
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
            loadedAlgorithms = new ArrayList<>();
            Arrays.asList(carlab.android_passthroughs.Algorithm.class);
            loadedFunctions = Arrays.asList(carlab.android_passthroughs.Algorithm.getLocation);
            saveInformation = Arrays.asList(Registry.Location);
        }
    }
}
