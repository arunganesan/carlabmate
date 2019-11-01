package edu.umich.aligned_imu;

import android.content.Context;

import java.util.ArrayList;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.Algorithm;
import edu.umich.carlab.loadable.AlgorithmSpecs;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class AlignedIMUBase extends Algorithm {
    final String ALIGNED_ACCEL = "world-aligned-accel";
    final String ALIGNED_GYRO = "world-aligned-gyro";
    final String ROTATION = "rotation";
    private boolean calculatedRotation = false;

    /*[{
        "module": "aligned-imu",
        "classname": "AlignedIMU",
        "functions": [
            {
                "output": "rotation",
                    "input": ["gravity", "magnetometer"]
            },{
                "output": "world-aligned-gyro",
                        "input": ["gyro", "rotation"]
            },{
                "output": "world-aligned-accel",
                        "input": ["accel", "rotation"]
            }
        ]
    }]*/
    private Float[] lastMagnet, lastGravity, lastGyro, lastAccel, lastRotation;

    public AlignedIMUBase (CLDataProvider cl, Context context) {
        super(cl, context);

        name = "world_aligned_imu";
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GRAVITY);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.MAGNET);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GYRO);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.ACCEL);

        algorithmFunctions = new ArrayList<>();
        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction("rotation", "gravity", "magnetometer"));

        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction("world-aligned-gyro", "gyro", "rotation"));

        algorithmFunctions
                .add(new AlgorithmSpecs.AppFunction("world-aligned-accel", "accel", "rotation"));
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
            case ROTATION:
                lastRotation = (Float[]) dObject.value;
                break;
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
            if (lastGravity != null && lastMagnet != null)
                outputData(ROTATION, produceRotation(lastMagnet, lastGravity));
        } else if (sensor.equals(PhoneSensors.GYRO) || sensor.equals(ROTATION)) {
            if (lastRotation != null && lastGyro != null)
                outputData(ALIGNED_GYRO, produceAlignedGyro(lastGyro, lastRotation));
        } else if (sensor.equals(PhoneSensors.ACCEL) || sensor.equals(ROTATION)) {
            if (lastAccel != null && lastRotation != null)
                outputData(ALIGNED_ACCEL, produceAlignedAccel(lastAccel, lastRotation));
        }
    }

    public abstract Float[] produceAlignedAccel (Float[] accel, Float[] rm);

    public abstract Float[] produceAlignedGyro (Float[] gyro, Float[] rm);

    public abstract Float[] produceRotation (Float[] m, Float[] g);
}
