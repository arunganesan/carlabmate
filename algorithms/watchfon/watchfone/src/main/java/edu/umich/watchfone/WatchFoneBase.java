package edu.umich.watchfone;

import android.content.Context;

import java.util.ArrayList;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class WatchFoneBase extends Algorithm {
    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    public WatchFoneBase (CLDataProvider cl, Context context) {
        super(cl, context);

        name = "watchfone";

        algorithmFunctions = new ArrayList<>();
        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction(new AlgorithmSpecs.InfoCarSpeed(false),
                                                    new AlgorithmSpecs.InfoWorldAlignedAccel(false),
                                                    new AlgorithmSpecs.InfoGPS(false)));


        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction(new AlgorithmSpecs.InfoCarSteering(false),
                                                    new AlgorithmSpecs.InfoCarSpeed(false),
                                                    new AlgorithmSpecs.InfoWorldAlignedGyro(false)));

        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction(new AlgorithmSpecs.InfoCarFuel(false),
                                                    new AlgorithmSpecs.InfoCarOdometer(false)));

        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction(new AlgorithmSpecs.InfoCarOdometer(false),
                                                    new AlgorithmSpecs.InfoGPS(false)));

        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction(new AlgorithmSpecs.InfoCarRPM(false),
                                                    new AlgorithmSpecs.InfoCarSpeed(false),
                                                    new AlgorithmSpecs.InfoCarGear(false)));


        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction(new AlgorithmSpecs.InfoCarGear(false),
                                                    new AlgorithmSpecs.InfoCarSpeed(false)));
    }

    @Override
    public void newData (DataMarshal.DataObject dObject) {
        super.newData(dObject);
        String sensor = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        switch (sensor) {
            case PhoneSensors.GRAVITY:
                lastGravity = (Float[]) dObject.value;
                break;
            case PhoneSensors.MAGNET:
                lastMagnet = (Float[]) dObject.value;
                break;
            case PhoneSensors.GYRO:
                lastGyro = (Float[]) dObject.value;
                break;
            case PhoneSensors.ACCEL:
                lastAccel = (Float[]) dObject.value;
                break;
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        // if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
        //     if (lastGravity != null && lastMagnet != null)
        //         outputData(ROTATION, produceRotation(lastMagnet, lastGravity));
        // } else if (sensor.equals(PhoneSensors.GYRO) || sensor.equals(ROTATION)) {
        //     if (lastRotation != null && lastGyro != null)
        //         outputData(ALIGNED_GYRO, produceAlignedGyro(lastGyro, lastRotation));
        // } else if (sensor.equals(PhoneSensors.ACCEL) || sensor.equals(ROTATION)) {
        //     if (lastAccel != null && lastRotation != null)
        //         outputData(ALIGNED_ACCEL, produceAlignedAccel(lastAccel, lastRotation));
        // }
    }

    public abstract Float produceCarSpeed (Float[] aligned_accel, Float[] gps);
    public abstract Float produceCarSteering (Float[] aligned_gyro, Float car_speed);
    public abstract Float produceCarFuel (Float[] car_odometer);
    public abstract Float produceCarGear (Float car_speed);
    public abstract Float produceCarOdometer (Float[] gps);
    public abstract Float produceCarRPM (Float [] car_gear, Float[] car_speed);
}
