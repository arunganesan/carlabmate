package edu.umich.carlab.loadable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmSpecs {
    public static Map<String, Object> InformationDatatypes;
    static {
        InformationDatatypes = new HashMap<>();
        InformationDatatypes.put("rotation", new float[3][3]);
        InformationDatatypes.put("gravity", new float[3]);
        InformationDatatypes.put("world-aligned-gyro", new float[3]);
        InformationDatatypes.put("world-aligned-accel", new float[3]);
        InformationDatatypes.put("accel", new float[3]);
        InformationDatatypes.put("gyro", new float[3]);
        InformationDatatypes.put("magnetometer", new float[3]);
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
