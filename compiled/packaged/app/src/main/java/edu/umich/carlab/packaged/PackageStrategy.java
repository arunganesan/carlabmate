package edu.umich.carlab.packaged;

import java.util.Arrays;
import java.util.List;

import edu.umich.aligned_imu.AlignedIMU;
import edu.umich.carlab.Registry;
import edu.umich.carlab.Strategy;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.watchfone.WatchFone;

public class PackageStrategy extends Strategy {
    public List<Class<? extends Algorithm>> loadedAlgorithms =
            Arrays.asList(AlignedIMU.class, WatchFone.class);

    public List<Algorithm.Function> loadedFunctions =
            Arrays.asList(AlignedIMU.ProduceRotation, AlignedIMU.ProduceAlignedGyro,
                          AlignedIMU.ProduceAlignedAccel, WatchFone.produceCarFuel,
                          WatchFone.produceCarSpeed, WatchFone.produceCarSteering,
                          WatchFone.produceCarGear, WatchFone.produceCarOdometer,
                          WatchFone.produceCarRPM);

    public List<Registry.Information> saveInformation =
            Arrays.asList(Registry.WorldAlignedGyro, Registry.WorldAlignedAccel);
}
