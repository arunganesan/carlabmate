package edu.umich.carlab.packaged;

import java.util.Arrays;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.Registry;
import edu.umich.carlab.Strategy;
import edu.umich.watchfone.WatchFone;

public class PackageStrategy extends Strategy {
    public PackageStrategy () {
        loadedAlgorithms = Arrays.asList(AlignedIMU.class, WatchFone.class);

        loadedFunctions = Arrays.asList(AlignedIMU.ProduceRotation, AlignedIMU.ProduceAlignedGyro,
                                        AlignedIMU.ProduceAlignedAccel, WatchFone.produceCarFuel,
                                        WatchFone.produceCarSpeed, WatchFone.produceCarSteering,
                                        WatchFone.produceCarGear, WatchFone.produceCarOdometer,
                                        WatchFone.produceCarRPM);

        saveInformation = Arrays.asList(Registry.WorldAlignedGyro, Registry.WorldAlignedAccel);
    }
}
