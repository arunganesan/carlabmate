package edu.umich.carlab;

import android.hardware.Sensor;
import android.renderscript.Float2;
import android.renderscript.Float3;

import java.util.Arrays;

import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.utils.DevSen;

public class Registry {
    public static Information Accel =
            new Information("accel", Float3.class, Sensor.TYPE_ACCELEROMETER,
                            Sensor.STRING_TYPE_ACCELEROMETER,
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.ACCEL));
    public static Information CarFuel = new Information("car-fuel", Float.class);
    public static Information CarGear = new Information("car-gear", Float.class);
    public static Information CarModel = new Information("car-model", String.class);
    public static Information CarSpeed = new Information("car-speed", Float.class);
    public static Information CarSteering = new Information("car-steering", Float.class);
    public static Information GPS = new Information("gps", Float.class, 1, null,
                                                    new DevSen(PhoneSensors.DEVICE,
                                                               PhoneSensors.GPS));
    public static Information GearModelFile = new Information("gear-model-file", String.class);
    public static Information Gravity =
            new Information("gravity", Float3.class, Sensor.TYPE_GRAVITY,
                            Sensor.STRING_TYPE_GRAVITY,
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.GRAVITY));
    public static Information GravityAlignedGyro =
            new Information("gravity-aligned-gyro", Float.class);
    public static Information Gyro = new Information("gyro", Float3.class, Sensor.TYPE_GYROSCOPE,
                                                     Sensor.STRING_TYPE_GYROSCOPE,
                                                     new DevSen(PhoneSensors.DEVICE,
                                                                PhoneSensors.GYRO));
    // "map-matched-location": {"type": "string,float", "description": "Road name, percentage into road"},
    public static Information Location = new Information("location", Float2.class);
    public static Information Magnetometer =
            new Information("magnetometer", Float3.class, Sensor.TYPE_MAGNETIC_FIELD,
                            Sensor.STRING_TYPE_MAGNETIC_FIELD,
                            new DevSen(PhoneSensors.DEVICE, PhoneSensors.MAGNET));
    public static Information PhoneNumber = new Information("phone-numner", String.class);
    public static Information VehicleAlignedAccel =
            new Information("vehicle-aligned-accel", Float3.class);
    public static Information VehiclePointingRotation =
            new Information("vehicle-pointing-rotation", Float[].class);
    public static Information WorldAlignedAccel =
            new Information("world-aligned-accel", Float3.class);
    public static Information WorldAlignedGyro =
            new Information("world-aligned-gyro", Float3.class);
    public static Information WorldPointingRotation =
            new Information("world-pointing-rotation", Float[].class);

    public static class Information {
        public Class<?> dataType;
        public DevSen devSensor;
        public int lowLevelSensor;
        public String lowLevelSensorName;
        public String name;

        public Information (String n, Class<?> dt) {
            this(n, dt, -1, null, null);
        }

        public Information (String n, Class<?> dt, int lls, String llsn, DevSen ds) {
            name = n;
            dataType = dt;
            lowLevelSensor = lls;
            lowLevelSensorName = llsn;
            devSensor = ds;
        }

        @Override
        public boolean equals (Object other) {
            if (!other.getClass().equals(Information.class)) return false;
            return ((Information) other).name.equals(name);
        }
    }


    public static String FormatString (DataMarshal.DataObject dataObject) {
        Information information = dataObject.information;
        Object value = dataObject.value;
        String valString = "";

        if (information.dataType.equals(Float3.class)) {
            Float3 obj = (Float3)value;
            valString = Arrays.toString(new Float[] {
                    obj.x, obj.y, obj.z
            });
        }else if (information.dataType.equals(Float2.class)) {
            Float2 obj = (Float2)value;
            valString = Arrays.toString(new Float[] {
                    obj.x, obj.y
            });
        }
        else if (information.dataType.equals(Float.class)) {
            valString = "" + value;
        } else if (information.dataType.equals(String.class)) {
            valString = (String)value;
        } else if (information.dataType.equals(Float[].class)) {
            valString = Arrays.toString((Float[]) value);
        }

        return valString;
    }
}