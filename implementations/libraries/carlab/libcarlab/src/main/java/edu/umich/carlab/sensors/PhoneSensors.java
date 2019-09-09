package edu.umich.carlab.sensors;

import android.hardware.Sensor;
import edu.umich.carlab.DataMarshal;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by arunganesan on 2/12/18.
 */

public class PhoneSensors {
    public final static String DEVICE = "phone";

    // https://developer.android.com/reference/android/hardware/SensorEvent.html#values
    // Only the full data set can be subscribed.
    // Before saving, we will split them apart but not right now.

    public final static String ACCEL = "accel";
    public final static String GYRO = "gyro";
    public final static String MAGNET = "magnet";
    public final static String GRAVITY = "gravity";
    public final static String ORIENT = "orientation";
    public final static String GPS = "gps";

    // The following are used when accessing individual parts of the dataset
    public final static String ACCEL_X = "accel_x";
    public final static String ACCEL_Y = "accel_y";
    public final static String ACCEL_Z = "accel_z";
    public final static String GYRO_X = "gyro_x";
    public final static String GYRO_Y = "gyro_y";
    public final static String GYRO_Z = "gyro_z";
    public final static String MAGNET_X = "magnet_x";
    public final static String MAGNET_Y = "magnet_y";
    public final static String MAGNET_Z = "magnet_z";
    public final static String GRAVITY_X = "gravity_x";
    public final static String GRAVITY_Y = "gravity_y";
    public final static String GRAVITY_Z = "gravity_z";
    public final static String ORIENT_AZIMUTH = "orientation_azimuth";
    public final static String ORIENT_PITCH = "orientation_pitch";
    public final static String ORIENT_ROLL = "orientation_roll";

    // Sensors accessed differently than the above IMU sensors
    public final static String GPS_LATITUDE = "gps_latitude";
    public final static String GPS_LONGITUDE = "gps_longitude";
    public final static String GPS_SPEED = "gps_speed";


    /**
     * Convenience function which says if the sensor is a broadcast-type sensor
     * If it is, then we can register a broadcast receiver and call {@link #sensorNameToType(String)}
     *
     * @param name
     * @return
     */
    public static boolean isBroadcastSensor(String name) {
        return !name.equals(GPS);
    }


    /**
     * Split the sensor names
     *
     * @param aggregateSensor
     * @return
     */
    public static String[] splitSensorNames(String aggregateSensor) {
        switch (aggregateSensor) {
            case ACCEL:
                return new String[]{ACCEL_X, ACCEL_Y, ACCEL_Z};
            case GYRO:
                return new String[]{GYRO_X, GYRO_Y, GYRO_Z};
            case MAGNET:
                return new String[]{MAGNET_X, MAGNET_Y, MAGNET_Z};
            case GRAVITY:
                return new String[]{GRAVITY_X, GRAVITY_Y, GRAVITY_Z};
            case ORIENT:
                return new String[]{ORIENT_AZIMUTH, ORIENT_PITCH, ORIENT_ROLL};
            case GPS:
                return new String[]{GPS_LATITUDE, GPS_LONGITUDE, GPS_SPEED};
        }

        return new String[]{aggregateSensor};
    }


    public static String sensorToAggregate(String sensor) {
        switch (sensor) {
            case ACCEL_X:
            case ACCEL_Y:
            case ACCEL_Z:
                return ACCEL;
            case GYRO_X:
            case GYRO_Y:
            case GYRO_Z:
                return GYRO;
            case MAGNET_X:
            case MAGNET_Y:
            case MAGNET_Z:
                return MAGNET;
            case GRAVITY_X:
            case GRAVITY_Y:
            case GRAVITY_Z:
                return GRAVITY;
            case ORIENT_AZIMUTH:
            case ORIENT_PITCH:
            case ORIENT_ROLL:
                return ORIENT;
            case GPS_LATITUDE:
            case GPS_LONGITUDE:
            case GPS_SPEED:
                return GPS;
        }

        return null;
    }

    public static String[] listAllSensors() {
        return new String[]{
                ACCEL,
                GYRO,
                MAGNET,
                GRAVITY,
                ORIENT,
                GPS
        };
    }


    public static boolean validSensor(String name) {
        return (name.equals(ACCEL)
                || name.equals(GYRO)
                || name.equals(MAGNET)
                || name.equals(GRAVITY)
                || name.equals(ORIENT)
                || name.equals(GPS)
        );
    }

    /**
     * This function splits an grouped together sensor source to it's complementary parts
     *
     * @param dataObject
     * @return
     */
    public static Map<String, Float> splitValues(DataMarshal.DataObject dataObject) {
        Map<String, Float> splitMap = new HashMap<>();
        String device = dataObject.device;
        if (!device.equals(DEVICE)) return splitMap;

        String sensor = dataObject.sensor;
        Float[] values;

        // This happens for status messages
        // We still want to split and broadcast, though
        if (dataObject.value.length == 1) {
            values = new Float[]{
                    dataObject.value[0],
                    dataObject.value[0],
                    dataObject.value[0]
            };
        } else {
            values = dataObject.value;
        }


        switch (sensor) {
            case ACCEL:
                splitMap.put(ACCEL_X, values[0]);
                splitMap.put(ACCEL_Y, values[1]);
                splitMap.put(ACCEL_Z, values[2]);
                break;
            case GYRO:
                splitMap.put(GYRO_X, values[0]);
                splitMap.put(GYRO_Y, values[1]);
                splitMap.put(GYRO_Z, values[2]);
                break;
            case MAGNET:
                splitMap.put(MAGNET_X, values[0]);
                splitMap.put(MAGNET_Y, values[1]);
                splitMap.put(MAGNET_Z, values[2]);
                break;
            case GRAVITY:
                splitMap.put(GRAVITY_X, values[0]);
                splitMap.put(GRAVITY_Y, values[1]);
                splitMap.put(GRAVITY_Z, values[2]);
                break;
            case ORIENT:
                splitMap.put(ORIENT_AZIMUTH, values[0]);
                splitMap.put(ORIENT_PITCH, values[1]);
                splitMap.put(ORIENT_ROLL, values[2]);
                break;
            case GPS:
                splitMap.put(GPS_LATITUDE, values[0]);
                splitMap.put(GPS_LONGITUDE, values[1]);
                splitMap.put(GPS_SPEED, values[2]);
        }
        return splitMap;
    }

    /**
     * Returns the sensor type that we have to register for to receive this sensor.
     * Register for sensor types often gives us a multi-dimensional value (e.g. [ax, ay, az])
     * but apps can register for just single dimensional values (e.g. just ax).
     *
     * @param name
     * @return
     */
    public static int sensorNameToType(String name) {
        switch (name) {
            case ACCEL:
                return Sensor.TYPE_ACCELEROMETER;
            case GYRO:
                return Sensor.TYPE_GYROSCOPE;
            case MAGNET:
                return Sensor.TYPE_MAGNETIC_FIELD;
            case GRAVITY:
                return Sensor.TYPE_GRAVITY;
            case ORIENT:
                return Sensor.TYPE_ORIENTATION;
        }
        return -999;
    }


    /**
     * This function is used inside PhoneController when its time to emit the data
     * back to the clients. If the app only registers for Accel_X, and we get new accelerometer
     * data, then PhoneController calls this function to get the list of sensors that
     * accelerometer data maps to. Then it can decide whether it should broadcast all of them
     * or just part of them.
     *
     * @param type
     * @return
     */
    public static String typeToSensorName(int type) {
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                return ACCEL;
            case Sensor.TYPE_GYROSCOPE:
                return GYRO;
            case Sensor.TYPE_MAGNETIC_FIELD:
                return MAGNET;
            case Sensor.TYPE_GRAVITY:
                return GRAVITY;
            case Sensor.TYPE_ORIENTATION:
                return ORIENT;
        }

        return null;
    }


    // This mapping is from: https://developer.android.com/reference/android/hardware/SensorEvent.html#values
    // https://developer.android.com/guide/topics/sensors/sensors_position.html
    private static float getSensorFromValues(float[] sensorValues, String sensorName) {
        if (sensorName.equals(ACCEL_X)
                || sensorName.equals(GYRO_X)
                || sensorName.equals(MAGNET_X)
                || sensorName.equals(GRAVITY_X)
                || sensorName.equals(ORIENT_AZIMUTH)) {
            return sensorValues[0];
        }


        if (sensorName.equals(ACCEL_Y)
                || sensorName.equals(GYRO_Y)
                || sensorName.equals(MAGNET_Y)
                || sensorName.equals(GRAVITY_Y)
                || sensorName.equals(ORIENT_PITCH)) {
            return sensorValues[1];
        }


        if (sensorName.equals(ACCEL_Z)
                || sensorName.equals(GYRO_Z)
                || sensorName.equals(MAGNET_Z)
                || sensorName.equals(GRAVITY_Z)
                || sensorName.equals(ORIENT_ROLL)) {
            return sensorValues[2];
        }

        return 0;
    }
}
