package edu.umich.carlab.packaged;

import java.util.Arrays;

import carlab.android_passthroughs.Algorithm;
import edu.umich.carlab.Registry;
import edu.umich.carlab.Strategy;

public class PackageStrategy extends Strategy {
    public PackageStrategy () {
        loadedAlgorithms = Arrays.asList(carlab.android_passthroughs.Algorithm.class,
                                         carlab.obd_devices.Algorithm.class);
        loadedFunctions =
                Arrays.asList(Algorithm.getLocation, carlab.obd_devices.Algorithm.readFuelLevel);
        saveInformation = Arrays.asList(Registry.Location, Registry.CarFuel);
    }
}
