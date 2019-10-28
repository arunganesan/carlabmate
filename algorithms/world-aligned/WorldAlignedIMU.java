package edu.umich.carlab.world_aligned_imu;

import android.content.Context;
import android.hardware.SensorManager;
import edu.umich.carlab.CLDataProvider;
import edu.umich.carlab.DataMarshal;
import edu.umich.carlab.sensors.PhoneSensors;
import edu.umich.carlabui.appbases.SensorListAppBase;

public class WorldAlignedIMU {
    float[][] RotMat = new float[3][3];

    Float [] magnet, gravity, gyro, accel;
    private float[] R = new float[9];
    private float[] I = new float[9];
    private float[] lastComputedOrientation = new float[3];

    public void getRotation (Float [] magnet, Float [] gravity) {
        if (magnet == null || gravity == null) return;

        float[] magnet_r = new float[]{magnet[0], magnet[1], magnet[2]};
        float[] gravity_r = new float[]{gravity[0], gravity[1], gravity[2]};


        boolean success = SensorManager.getRotationMatrix(R, I, gravity_r, magnet_r);

        if (success) {
            SensorManager.getOrientation(R, lastComputedOrientation);

            RotMat[0][0] = R[0];
            RotMat[0][1] = R[1];
            RotMat[0][2] = R[2];
            RotMat[1][0] = R[3];
            RotMat[1][1] = R[4];
            RotMat[1][2] = R[5];
            RotMat[2][0] = R[6];
            RotMat[2][1] = R[7];
            RotMat[2][2] = R[8];


            Float[] casted = new Float[3];
            casted[0] = lastComputedOrientation[0];
            casted[1] = lastComputedOrientation[1];
            casted[2] = lastComputedOrientation[2];
            outputData(MiddlewareImpl.APP, dObject, MiddlewareImpl.ORIENT, casted);
        }
    }


    public void alignedGyro (Float[] lastComputedOrientation, Float [] gyro) {
        outputData(
            MiddlewareImpl.APP,
            dObject,
            MiddlewareImpl.GYRO,
            MatrixMul(gyro, RotMat));
    }

    public void alignedAccel (Float[] lastComputedOrientation, Float [] accel) {
        outputData(
            MiddlewareImpl.APP,
            dObject,
            MiddlewareImpl.ACCEL,
            MatrixMul(accel, RotMat));
    }



    void getRotationMatrix(float[][] rm, Float[] m, Float[] g) {
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
