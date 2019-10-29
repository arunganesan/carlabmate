package edu.umich.aligned_imu;

import android.content.Context;

import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.loadable.App;
import edu.umich.carlab.sensors.PhoneSensors;

public class Algorithm extends App {
    private DataMarshal.DataObject lastMagnet,
            lastGravity,
            lastGyro,
            lastAccel;

    private boolean calculatedRotation = false;

    public Algorithm(CLDataProvider cl, Context context) {
        super(cl, context);

        name = "world_aligned_imu";
//        middlewareName = MiddlewareImpl.APP;
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GRAVITY);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.MAGNET);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.GYRO);
        subscribe(PhoneSensors.DEVICE, PhoneSensors.ACCEL);
    }


    @Override
    public void newData(DataMarshal.DataObject dObject) {
        super.newData(dObject);

        long time = dObject.time;
        String sensor = dObject.sensor;

        if (dObject.dataType != DataMarshal.MessageType.DATA) return;
//        if (dObject.device.equals(MiddlewareImpl.APP)) return;
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

        0. Take input and call the function
        1. Get the return value and save it
        2. ... Um that's it?
         */

        if (sensor.equals(PhoneSensors.MAGNET) || sensor.equals(PhoneSensors.GRAVITY)) {
            if (lastMagnet != null && lastGravity != null) {
                produceRotation(lastMagnet.value, lastGravity.value);
                calculatedRotation = true;
            }
        } else if (sensor.equals(PhoneSensors.GYRO) && calculatedRotation)
            produceAlignedGyro(lastGyro.value);
        else if (sensor.equals(PhoneSensors.ACCEL) && calculatedRotation)
            produceAlignedGyro(lastAccel.value);
    }



    float[][] rm = new float[3][3];
    public float[][] produceRotation (Float [] m, Float [] g) {
        // Cross product of magnet and gravity
        float[] c = { m[1]*g[2]-m[2]*g[1] , m[2]*g[0]-m[0]*g[2], m[0]*g[1]-m[1]*g[0]};

        // Divide by norm
        float norm = (float)Math.sqrt(c[0]*c[0] + c[1]*c[1] + c[2]*c[2]);
        c[0] /= norm; c[1] /= norm; c[2] /= norm;
        norm = (float)Math.sqrt(g[0]*g[0] + g[1]*g[1] + g[2]*g[2]);
        g[0] /= norm; g[1] /= norm; g[2] /= norm;

        // Reconstruct the magnet using another cross product
        float[] nm = { g[1]*c[2]-g[2]*c[1], g[2]*c[0]-g[0]*c[2], g[0]*c[1]-g[1]*c[0] };

        // Assign values
        rm[0][0] = c[0]; rm[0][1] = c[1]; rm[0][2] = c[2];
        rm[1][0] = nm[0]; rm[1][1] = nm[1]; rm[1][2] = nm[2];
        rm[2][0] = g[0]; rm[2][1] = g[1]; rm[2][2] = g[2];

        return rm;
    }

    public Float[] produceAlignedGyro (Float [] gyro) {
        return MatrixMul(gyro, rm);
    }

    public Float[] produceAlignedAccel (Float [] accel) {
        return MatrixMul(accel, rm);
    }

    public Float[] MatrixMul(Float[] T, float[][] RotMat) {
        Float[] temp = T.clone();
        for (int i = 0; i < RotMat.length; i++)
            for (int j = 0; j < T.length; j++) {
                temp[i] = temp[i] + T[j] * RotMat[j][i];
            }
        return temp;
    }
}
