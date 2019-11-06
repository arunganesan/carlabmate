package edu.umich.carlab;

import android.hardware.Sensor;

import java.io.Serializable;

import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.utils.DevSen;

public class Registry {
    public static Information Accel =
            new Information("accel", new float[3], Sensor.TYPE_ACCELEROMETER,
                            Sensor.STRING_TYPE_ACCELEROMETER,
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.ACCEL));
    public static Information CarFuel = new Information("car-fuel", 0F);
    public static Information CarGear = new Information("car-fuel", 0F);
    public static Information CarOdometer = new Information("car-fuel", 0F);
    public static Information CarRPM = new Information("car-fuel", 0F);
    public static Information CarSpeed = new Information("car-fuel", 0F);
    public static Information CarSteering = new Information("car-fuel", 0F);
    public static Information Gravity =
            new Information("gravity", new float[3], Sensor.TYPE_GRAVITY,
                            Sensor.STRING_TYPE_GRAVITY,
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.GRAVITY));
    public static Information Gyro = new Information("gyro", new float[3], Sensor.TYPE_GYROSCOPE,
                                                     Sensor.STRING_TYPE_GYROSCOPE,
                                                     new DevSen(PhoneSensors.DEVICE,
                                                                PhoneSensors.GYRO));
    public static Information Magnetometer =
            new Information("magnetometer", new float[3], Sensor.TYPE_MAGNETIC_FIELD,
                            Sensor.STRING_TYPE_MAGNETIC_FIELD,
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.MAGNET));
    public static Information Rotation = new Information("rotation", new float[9]);
    public static Information WorldAlignedGyro =
            new Information("world-aligned-gyro", new float[3]);
    public static Information WorldAlignedAccel =
            new Information("world-aligned-accel", new float[3]);

    // TODO what is the raw sensor here?
    public static Information GPS = new Information("gps", 0F);

    public static class Information {
        public Serializable dataType;
        public DevSen devSensor;
        public int lowLevelSensor;
        public String lowLevelSensorName;
        public String name;

        public Information (String n, Serializable dt) {
            this(n, dt, -1, null, null);
        }

        public Information (String n, Serializable dt, int lls, String llsn, DevSen ds) {
            name = n;
            dataType = dt;
            lowLevelSensor = lls;
            lowLevelSensorName = llsn;
            devSensor = ds;
        }

        @Override
        public boolean equals (Object other) {
            if (!other.getClass().equals(Information.class))
                return false;
            return ((Information)other).name.equals(name);
        }
    }
}