package edu.umich.aligned_imu;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

public abstract class Algorithm extends App {
    private DataMarshal.DataObject lastMagnet,
            lastGravity,
            lastGyro,
            lastAccel;

    float [] [] lastRotation = null;


    final String ROTATION = "rotation";
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
        String sensor = dObject.sensor;
        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
        if (dObject.value == null) return;

        switch (sensor) {
            case PhoneSensors.GRAVITY:
                lastGravity = dObject;
                break;
            case PhoneSensors.MAGNET:
                lastMagnet = dObject;
                break;
            case PhoneSensors.GYRO:
                lastGyro = dObject;
                break;
            case PhoneSensors.ACCEL:
                lastAccel = dObject;
                break;
        }


        /*
        Need to go from the JSON description of the IO of this algorithm to the following functions
         */

        if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
            if (lastGravity != null && lastMagnet != null)
                produceRotation(lastMagnet.value, lastGravity.value);
        } else if (sensor.equals(PhoneSensors.GYRO) || sensor.equals(ROTATION))
            if (lastRotation != null && lastGyro != null)
                produceAlignedGyro(lastGyro.value, null);
            else if (sensor.equals(PhoneSensors.ACCEL) || sensor.equals(ROTATION))
                if (lastAccel != null && lastRotation != null)
                    produceAlignedAccel(lastAccel.value, null);
    }



    public abstract float[][] produceRotation (Float [] m, Float [] g);

    public abstract Float[] produceAlignedGyro (Float [] gyro, float [][] rm);

    public abstract Float[] produceAlignedAccel (Float [] accel, float [] [] rm);
}
