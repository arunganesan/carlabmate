package edu.umich.aligned_imu;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;

public class WatchFon extends Algorithm {
    public WatchFon(CLDataProvider cl, Context context) {
        super(cl, context);
    }

    @Override
    public float produceCarSpeed (float[] location, float[] worldAlignedAccel) {
        return 0;
    }

    @Override
    public float produceCarSteering (float carSpeed, float[] worldAlignedGyro, String carModel) {
        return 0;
    }

    @Override
    public int produceCarGear (float carSpeed, String carModel) {
        return 0;
    }

    @Override
    public float produceCarOdometer (float[] location) {
        return 0;
    }

    @Override
    public float produceCarFuel (float odometer, String carModel, String mapMatchedLocation0, String mapMatchedLocation1, Float mapMatchedLocation2) {
        return 0;
    }

    @Override
    public float produceCarRpm (float carSpeed, int carGear, String carModel) {
        return 0;
    }
}
