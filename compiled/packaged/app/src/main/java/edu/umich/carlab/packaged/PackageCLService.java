package edu.umich.carlab.packaged;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Arrays;
import java.util.List;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.Constants;
import edu.umich.carlab.Registry;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.watchfone.WatchFone;

public class PackageCLService extends edu.umich.carlab.CLService {
    @Override
    protected void loadRequirements () {
        strategy = new PackageStrategy();
    }

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
}
