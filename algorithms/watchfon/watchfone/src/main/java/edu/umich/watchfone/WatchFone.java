package edu.umich.watchfone;

import android.content.Context;
import android.location.Location;

import java.util.Map;

import edu.umich.carlab.CLDataProvider;

public class WatchFone extends WatchFoneBase {
    final Double GALLONS_PER_LITER = 0.26417217685;
    final double IDLE_RPM = 800;
    final double INCHES_TO_METERS = 0.0254;
    final Double MILE_PER_KM = 0.621371;
    final float MPS_TO_KMPH = 1 / 0.621371f;
    final double TIRE_DIAM = (245 / 1000.0 * 45 / 100.0) * 2 + (18 * INCHES_TO_METERS);
    Float AVERAGE_MPG = 23f;
    float FINAL_DRIVE_RATIO = 3.36f;
    Map<Integer, Float> Gear_Ratio;
    Float MAX_FUEL_CAPACITY = 18f;
    float STEERING_RATIO = 14.8f;
    float TIRE_CIRCUM = (float) (TIRE_DIAM * Math.PI / 1000.0);
    float VEHICLE_LENGTH = (float) (193.9 * INCHES_TO_METERS); // Finally in meters
    Double distance = 0d;
    Double fuelConsumed;
    Float[] lastGPS;
    Integer lastGear = null;
    Location lastLoc, currLoc;
    Double lastOdometer, currOdometer;
    Float lastSpeed = null;
    Float lastYaw = null;
    Double previousFuelLevel; // in liters
    public WatchFone (CLDataProvider cl, Context context) {
        super(cl, context);
        name = "watchfone";
    }

    public Float produceCarFuel (Float[] car_odometer) {
        distance = currOdometer - lastOdometer; // in meters
        distance = distance / 1000 * MILE_PER_KM; // in miles;
        fuelConsumed = distance / AVERAGE_MPG / GALLONS_PER_LITER; // in gallons
        previousFuelLevel -= fuelConsumed;
        return previousFuelLevel.floatValue();
    }

    public Float produceCarGear (Float car_speed) {
        // TODO.
        return 0F;
    }

    public Float produceCarOdometer (Float[] gps) {
        lastLoc.setLatitude(lastGPS[0]);
        lastLoc.setLongitude(lastGPS[1]);
        currLoc.setLatitude(gps[0]);
        currLoc.setLongitude(gps[1]);
        distance += currLoc.distanceTo(lastLoc);
        return distance.floatValue();
    }

    public Float produceCarRPM (Float[] car_gear, Float[] car_speed) {
        Double rpm;
        rpm = (FINAL_DRIVE_RATIO * Gear_Ratio.get(lastGear)) / TIRE_CIRCUM / 60.0;
        if (rpm < IDLE_RPM) rpm = IDLE_RPM;
        return rpm.floatValue();
    }

    public Float produceCarSpeed (Float[] aligned_accel, Float[] gps) {
        return gps[2] * MPS_TO_KMPH;
    }

    public Float produceCarSteering (Float[] aligned_gyro, Float car_speed) {
        Double steering = (double) (lastSpeed / lastYaw);
        steering = Math.asin(VEHICLE_LENGTH / steering);
        steering = STEERING_RATIO * steering;
        steering *= 180 / Math.PI;
        return steering.floatValue();
    }
}
