package edu.umich.aligned_imu;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class Algorithm extends App {
    private float[] lastMagnet,
            lastGravity,
            lastGyro,
            lastAccel;

    float [] [] lastRotation = null;


    final String ROTATION = "rotation";
    final String ALIGNED_GYRO = "world-aligned-gyro";
    final String ALIGNED_ACCEL = "world-aligned-accel";

    private boolean calculatedRotation = false;

    public Algorithm(CLDataProvider cl, Context context) {
        super(cl, context);

        name = "world_aligned_imu";
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GRAVITY);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.MAGNET);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GYRO);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.ACCEL);
    }

    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);
        String sensor = dObject.information;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        switch (sensor) {
            case PhoneSensors.GRAVITY:
                lastGravity = (float[])dObject.value;
                break;
            case PhoneSensors.MAGNET:
                lastMagnet = (float[])dObject.value;
                break;
            case PhoneSensors.GYRO:
                lastGyro = (float[])dObject.value;
                break;
            case PhoneSensors.ACCEL:
                lastAccel = (float[])dObject.value;
                break;
            case ROTATION:
                lastRotation = (float[][])dObject.value;
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



    public abstract float[][] produceRotation (float [] m, float [] g);

    public abstract float[] produceAlignedGyro (float [] gyro, float [][] rm);

    public abstract float[] produceAlignedAccel (float [] accel, float [] [] rm);
}
