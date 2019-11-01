package edu.umich.carlab.loadable;

import android.hardware.Sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmSpecs {
    public static Map<String, Object> InformationDatatypes;
    public static Map<String, Integer> LowLevelSensors;

    static {
        InformationDatatypes = new HashMap<>();
        InformationDatatypes.put("rotation", new float[9]);
        InformationDatatypes.put("gravity", new float[3]);
        InformationDatatypes.put("world-aligned-gyro", new float[3]);
        InformationDatatypes.put("world-aligned-accel", new float[3]);
        InformationDatatypes.put("accel", new float[3]);
        InformationDatatypes.put("gyro", new float[3]);
        InformationDatatypes.put("magnetometer", new float[3]);

        LowLevelSensors = new HashMap<>();
        LowLevelSensors.put("rotation", Sensor.TYPE_ROTATION_VECTOR);
        LowLevelSensors.put("gravity", Sensor.TYPE_GRAVITY);
        LowLevelSensors.put("world-aligned-gyro", -1);
        LowLevelSensors.put("world-aligned-accel", -1);
        LowLevelSensors.put("accel", Sensor.TYPE_ACCELEROMETER);
        LowLevelSensors.put("gyro", Sensor.TYPE_GYROSCOPE);
        LowLevelSensors.put("magnetometer", Sensor.TYPE_MAGNETIC_FIELD);
    }

    public static class AppFunction {
        public String outputInformation;
        public List<String> inputInformation;

        public AppFunction(String outputInformation, String ... inputInformation) {
            this.outputInformation = outputInformation;
            this.inputInformation = Arrays.asList(inputInformation);
        }
    }
}