#! /usr/bin/env python3.7

"""
# REACT

import { acceptFuelLevel as AcceptFuelLevel } from "user-input";


  <AcceptFuelLevel
    produce={(fuelLevel: Number) => {
        this.libcarlab.outputNewInfo(
        new DataMarshal(Registry.CarFuel, fuelLevel), 
        () => {}
        );
    }}
    />



# PYTHON

import map_match

# per algorithm stuff
alg = map_match.algorithm.AlgorithmImpl()

loaded_functions: List[AlgorithmFunction] = [
    alg.mapmatch_function,
]



# XXX this needs to be used somewhere to actually save the data.
# But it may not be relevant for Python 
to_save_information: List[Information] = [
    Registry.MapMatchedLocation
]



JAVA, really its just this:

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

"""