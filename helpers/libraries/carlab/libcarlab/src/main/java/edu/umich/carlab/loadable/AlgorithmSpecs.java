package edu.umich.carlab.loadable;

import android.hardware.Sensor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlab.utils.DevSen;

public class AlgorithmSpecs {
    public static class AlgorithmInformation {
        public Algorithm algorithm;
        public Information information;

        public AlgorithmInformation (Algorithm a, Information i) {
            algorithm = a;
            information = i;
        }
    }

    public static class AppFunction {
        public List<Information> inputInformation;
        public Information outputInformation;

        public AppFunction (Information outputInformation, Information... inputInformation) {
            this.outputInformation = outputInformation;
            this.inputInformation = Arrays.asList(inputInformation);
        }
    }

    public static class InfoAccel extends Information {
        public InfoAccel (boolean save) {
            super("accel", new float[3], Sensor.TYPE_ACCELEROMETER,
                  Sensor.STRING_TYPE_ACCELEROMETER,
                  new DevSen(PhoneSensors.DEVICE, PhoneSensors.ACCEL));
            shouldSave = save;
        }
    }

    public static class InfoGravity extends Information {
        public InfoGravity (boolean save) {
            super("gravity", new float[3], Sensor.TYPE_GRAVITY, Sensor.STRING_TYPE_GRAVITY,
                  new DevSen(PhoneSensors.DEVICE, PhoneSensors.GRAVITY));
            shouldSave = save;
        }
    }

    public static class InfoGyro extends Information {
        public InfoGyro (boolean save) {
            super("gyro", new float[3], Sensor.TYPE_GYROSCOPE, Sensor.STRING_TYPE_GYROSCOPE,
                  new DevSen(PhoneSensors.DEVICE, PhoneSensors.GYRO));
            shouldSave = save;
        }
    }

    public static class InfoMagnetometer extends Information {
        public InfoMagnetometer (boolean save) {
            super("magnetometer", new float[3], Sensor.TYPE_MAGNETIC_FIELD,
                  Sensor.STRING_TYPE_MAGNETIC_FIELD,
                  new DevSen(PhoneSensors.DEVICE, PhoneSensors.MAGNET));
            shouldSave = save;
        }

    }

    public static class InfoRotation extends Information {
        public InfoRotation (boolean save) {
            super("rotation", new float[9]);
            shouldSave = save;
        }
    }

    public static class InfoWorldAlignedAccel extends Information {
        public InfoWorldAlignedAccel (boolean save) {
            super("world-aligned-accel", new float[3]);
            shouldSave = save;
        }
    }

    public static class InfoWorldAlignedGyro extends Information {
        public InfoWorldAlignedGyro (boolean save) {
            super("world-aligned-gyro", new float[3]);
            shouldSave = save;
        }
    }

    public static class Information {
        public Serializable dataType;
        public DevSen devSensor;
        public int lowLevelSensor;
        public String lowLevelSensorName;
        public String name;
        public boolean shouldSave;

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
    }
}